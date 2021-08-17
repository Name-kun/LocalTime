package xyz.namekun.localtime;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.TimeZone;

public class LocalTimeCmd implements CommandExecutor, TabCompleter {

    LocalTime plugin = LocalTime.getPlugin(LocalTime.class);

    HashMap<Player, String> timezoneMap = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("localtime")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("コンソールからかい？少し左を見てごらん、タイムスタンプがあるだろ？");
                return true;
            }
            Player p = (Player) sender;
            if (args.length >= 1) {
                if (LocalTime.config.getStringList("country").contains(args[0])) {
                    if (args.length == 1) {
                        sender.sendMessage("都市名を指定してください");
                    } else {
                        p.sendMessage("取得中です...");
                        try {
                            Document document = Jsoup.connect("https://www.timeanddate.com/worldclock/" + args[0] + "/" + args[1]).get();
                            //ローカルタイムの取得
                            Elements date = document.select("div#qlook.bk-focus__qlook p span#ctdat");
                            Elements time = document.select("div#qlook.bk-focus__qlook div span#ct.h1");
                            Elements timezone = document.select("div#qlook.bk-focus__qlook div span#cta");
                            sender.sendMessage(args[0] + "/" + args[1] + "のローカルタイム");
                            sender.sendMessage("ローカルタイム: " + date.text() + " " + time.text() + "(" + timezone.text() + ")");
                        } catch (IOException e) {
                            sender.sendMessage("エラーが発生しました。");
                        }
                    }
                } else {
                    Player target = Bukkit.getPlayerExact(args[0]);
                    if (target == null) {
                        p.sendMessage("指定したプレイヤーまたは国名は存在しないか、定義されていません。");
                    } else {
                        InetSocketAddress ip = target.getAddress();
                        p.sendMessage("取得中です...");
                        try {
                            p.sendMessage(target.getDisplayName() + "のローカルタイム");
                            getCountry(ip, p, target);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                        TimeZone timezone = TimeZone.getTimeZone(timezoneMap.get(target));
                        SimpleDateFormat sdf = new SimpleDateFormat();
                        Date date = new Date();
                        sdf.setTimeZone(timezone);
                        p.sendMessage("LocalTime: " + sdf.format(date));
                    }
                }
                return true;
            }
                InetSocketAddress ip = p.getAddress();
                p.sendMessage("取得中です...");
                try {
                    getCountry(ip, p, null);
                } catch (Exception exception) {
                    exception.printStackTrace();
                    sender.sendMessage("エラーが発生しました。");
                }
                TimeZone timezone = TimeZone.getTimeZone(timezoneMap.get(p));
                SimpleDateFormat sdf = new SimpleDateFormat();
                Date date = new Date();
                sdf.setTimeZone(timezone);
                p.sendMessage("LocalTime: " + sdf.format(date));
        } if (command.getName().equalsIgnoreCase("localtimereload")) {
            if (sender.hasPermission("localtimereload")) {
                plugin.createFiles();
                sender.sendMessage("コンフィグの再読込が完了しました。");
            }
        }
        return true;
    }

    public void getCountry(InetSocketAddress ip, Player p, Player target) throws Exception {
        URL url = new URL("http://ip-api.com/json/" + ip.getHostName());
        BufferedReader stream = new BufferedReader(new InputStreamReader(
                url.openStream()));
        StringBuilder entirePage = new StringBuilder();
        String inputLine;
        while ((inputLine = stream.readLine()) != null) entirePage.append(inputLine);
        stream.close();
        if (!(entirePage.toString().contains("\"country\":\""))) {
            p.sendMessage("Country: unknown");
        } else p.sendMessage("Country: " + entirePage.toString().split("\"country\":\"")[1].split("\",")[0]);
        if (!(entirePage.toString().contains("\"timezone\":\""))) {
            p.sendMessage("Timezone: unknown");
        } else {
            String timezone = entirePage.toString().split("\"timezone\":\"")[1].split("\",")[0];
            if (target == null) timezoneMap.put(p, timezone);
            else timezoneMap.put(target, timezone);
            p.sendMessage("Timezone: " + timezone);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> tab = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("localtime")) {
            if (args.length == 1) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    tab.add(p.getDisplayName());
                }
                tab.addAll(LocalTime.config.getStringList("country"));
            }
            else if (args.length == 2) {
                if (LocalTime.config.getStringList("country").contains(args[0])) {
                    try {
                        Document document = Jsoup.connect("https://www.timeanddate.com/worldclock/" + args[0]).get();
                        Elements cities = document.select("div.eight.columns figure.tzmp_fig.clearfix div a");
                        for (Element city : cities) {
                            tab.add(Normalizer.normalize(city.attr("title"), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")
                                    .replace(" ", "-").replace(".", "")
                                    .replace("/", "-").replace("'", ""));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return tab;
    }
}
