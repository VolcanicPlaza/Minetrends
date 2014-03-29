package com.volcanicplaza.MineTrends;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class Minetrends extends JavaPlugin {
	
	public static JavaPlugin plugin = null;
	public static String hostname = null;
	
	//Contains public and private.
	public static String key = null;
	
	public static String publicKey = null;
	public static String privateKey = null;
	
	public static int time = 0;
	public static BukkitTask runnable;
	
	@Override
	public void onEnable(){
		plugin = this;
		plugin.getConfig().options().copyDefaults(true);
		saveConfig();
		plugin.reloadConfig();
		
		hostname = "http://api.minetrends.com";
		
		refreshConfig();
		
		publicKey = Encryption.getServerKey();
		privateKey = Encryption.getPrivateKey();
		
		Bukkit.getLogger().info(getDescription().getName() + " v" + getDescription().getVersion() + " has been enabled!");
	}
	
	@Override
	public void onDisable(){
		Bukkit.getLogger().info(getDescription().getName() + " v" + getDescription().getVersion() + " has been disabled!");
		Bukkit.getScheduler().cancelAllTasks();
	}
	
	public static int getFrequency(){
		URL url = null;
		HttpURLConnection conn = null;
		int responseInt = 0;
		
		String urlParameters = "key=" + Minetrends.publicKey;
		  try {
			  url = new URL(Minetrends.hostname + "/api/getfrequency");
			  
		  } catch (Exception ex){
			  ex.printStackTrace();
		  }
		
		try {
			conn = (HttpURLConnection) url.openConnection();
		try {
			conn.setRequestMethod("POST"); //use post method
			conn.setDoOutput(true); //we will send stuff
			conn.setDoInput(true); //we want feedback
			conn.setUseCaches(false); //no caches
			conn.setConnectTimeout(2000); //set timeout
			conn.setInstanceFollowRedirects(true);
			conn.setAllowUserInteraction(false);
			conn.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
			conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
		}
			catch (ProtocolException e) {
			e.printStackTrace();
		}
		
		// Open a stream which can write to the URL
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
		
		// Open a stream which can read the server response
		InputStream in = conn.getInputStream();
		try {
			BufferedReader rd  = new BufferedReader(new InputStreamReader(in));
			String response = rd.readLine();
			
			if (response != null){
				//Check if the response is a int.
				if (response.equalsIgnoreCase("invalid-key")){
					responseInt = -1;
				} else {
					try {
				        responseInt = Integer.parseInt(response); 
				    } catch(NumberFormatException e) { 
				    	responseInt = 0;
				    }
				}
			} else {
				responseInt = 0;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally { //in this case, we are ensured to close the input stream
			if (in != null)
			in.close();
		}
		} catch (IOException e) {
		}finally {  //in this case, we are ensured to close the connection itself
			if (conn != null)
			conn.disconnect();
		}
		return responseInt;
		
	}
	
	public static void refreshConfig(){
		plugin.reloadConfig();
		key = plugin.getConfig().getString("key");
		
		privateKey = Encryption.getPrivateKey();
		publicKey = Encryption.getServerKey();
		
		int time = getFrequency();
		if (time == 0){
			//Received no data.
			Bukkit.getLogger().info("[URGENT] <Minetrends> Could not receive frequency data!");
			Bukkit.getLogger().info("[URGENT] <Minetrends> Will not send any data to Minetrends.");
		} else if (time == -1){
			//Invalid key
			Bukkit.getLogger().info("[URGENT] <Minetrends> Could not receive frequency data!");
			Bukkit.getLogger().info("[URGENT] <Minetrends> You have specified an INVALID SERVER KEY!");
			Bukkit.getLogger().info("[URGENT] <Minetrends> Will not send any data to Minetrends.");
		} else {
			Bukkit.getLogger().info("<Minetrends> Sucessfully received send frequency data.");
			Bukkit.getLogger().info("<Minetrends> Will send data to Minetrends every " + time + " seconds!");
			Minetrends.runnable = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new sendRunnable(), 20L, (20 * time));
		}
	}
	
	public static String getData(){
		ObjectMapper mapper = new ObjectMapper();
		
		List<Player> playersObj = new ArrayList<Player>();
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			playersObj.add(player);
		}
		
		Map<String,Object> data = new HashMap<String,Object>();
		Map<String,Object> playersList = new HashMap<String,Object>();
		Map<String,String> player = new HashMap<String,String>();
		
		for (Player plr : playersObj){
			//Player's IP Address
			player.put("ADDRESS", Encryption.encryptString(plr.getAddress().toString()));
			
			//Player's XP Level
			player.put("XPLEVELS", Encryption.encryptString(String.valueOf(plr.getLevel())));
			
			//Add to the main data array
			playersList.put(Encryption.encryptString(plr.getName()), player);
		}
		
		data.put("players", playersList);
		
		
		data.put("TIMEZONE", Encryption.encryptString(TimeZone.getDefault().getDisplayName()));
		data.put("TIMEZONEID", Encryption.encryptString(TimeZone.getDefault().getID()));
		data.put("TIMELOCAL", Encryption.encryptString(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime())));
		
		//Memory Usage
		data.put("memoryFree", "" + Encryption.encryptString(Runtime.getRuntime().freeMemory() + ""));
		data.put("memoryTotal", "" + Encryption.encryptString(Runtime.getRuntime().totalMemory() + ""));
		data.put("memoryMax", Encryption.encryptString(Runtime.getRuntime().maxMemory() + ""));
		
		//Diskspace Usage
		File hd = new File("/");
		data.put("diskspaceFree", "" + Encryption.encryptString(hd.getUsableSpace() + ""));
		data.put("diskspaceTotal", Encryption.encryptString(hd.getTotalSpace() + ""));
		
		//Installed plugin version.
		data.put("pluginVersion", plugin.getDescription().getVersion());
		
		if (Minetrends.privateKey == null){
			data.put("secure", false);
		} else {
			data.put("secure", true);
		}
		
		
		try {
			String result = mapper.writeValueAsString(data);
			return result;
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		//Define needed variables.
		PluginDescriptionFile pdfFile = getDescription();
		
		if (cmd.getName().equalsIgnoreCase("minetrends")){
			if (args.length == 0){
				sender.sendMessage(ChatColor.AQUA + "-=-=-=-=-=- Minetrends -=-=-=-=-=-");
				sender.sendMessage(ChatColor.AQUA + "/minetrends" + ChatColor.GRAY + " Shows this help page.");
				sender.sendMessage(ChatColor.AQUA + "/minetrends reload" + ChatColor.GRAY + " Reload the Minetrends configuration file.");
				sender.sendMessage(ChatColor.AQUA + "/minetrends key <server_key>" + ChatColor.GRAY + " Add your server key.");
				sender.sendMessage(ChatColor.AQUA + "-=-=-=-=-=-{ v" + pdfFile.getVersion() + " }-=-=-=-=-=-");
				return true;
			} else if (args.length == 1){
				if (args[0].equalsIgnoreCase("update")){
					if (sender.isOp()){
						sender.sendMessage(ChatColor.RED + "This feature has not yet been implemented.");
						return true;
					} else {
						sender.sendMessage(ChatColor.RED + "This feature has not yet been implemented.");
						return true;
					}
				} else if (args[0].equalsIgnoreCase("reload")){
					if (sender.isOp()){
						refreshConfig();
						sender.sendMessage(ChatColor.AQUA + "Minetrends configuration reloaded!");
					} else {
						sender.sendMessage(ChatColor.RED + "You do not have permissions to reload Minetrends.");
					}
					return true;
				} else if (args[0].equalsIgnoreCase("key")){
					if (sender.isOp()){
						sender.sendMessage(ChatColor.RED + "Incorrect Command Usage! " + ChatColor.AQUA + " /minecron key <server_key>");
						sender.sendMessage(ChatColor.GRAY + "You can find your server key from your Minetrends control panel.");
					} else {
						sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
					}
					return true;
				}
			} else if (args.length == 2){
				if (args[0].equalsIgnoreCase("key")){
					if (sender.isOp()){
						plugin.getConfig().set("key", args[1]);
						plugin.saveConfig();
						refreshConfig();
						sender.sendMessage(ChatColor.AQUA + "Your server key has been successfully added!");
					} else {
						sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
					}
					return true;
				}
			}
		}
		return false;
	}
}