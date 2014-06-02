package com.volcanicplaza.Minetrends;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

import com.volcanicplaza.Minetrends.Updater.UpdateResult;

public class Minetrends extends JavaPlugin {
	
	public static JavaPlugin plugin = null;
	public static String hostname = null;
	
	//Contains public and private.
	public static String key = null;
	
	public static String publicKey = null;
	public static String privateKey = null;
	
	public static int time = 0;
	public static BukkitTask runnable;
	
	//Updater Class
	public static UpdateResult update;
	public static String name = "";
	public static String version;
	
	@Override
	public void onEnable(){
		plugin = this;
		plugin.getConfig().options().copyDefaults(true);
		saveConfig();
		plugin.reloadConfig();
		
		hostname = "http://api.minetrends.com";
		//hostname = "http://192.168.1.33";
		
		refreshConfig();
		
		//Check if a new update is available.
		if (plugin.getConfig().getBoolean("check-updates")){
			Updater updater = new Updater(plugin, 76929, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
			update = updater.getResult();
			if (update == Updater.UpdateResult.UPDATE_AVAILABLE) {
				name = updater.getLatestName(); // Get the latest version
				version = updater.getLatestName().substring(updater.getLatestName().lastIndexOf('v') + 1);
				Bukkit.getLogger().info("***************************************************************");
				Bukkit.getLogger().info("There is a new Minetrends update available for download! (v" + version + ")");
				Bukkit.getLogger().info("Type /minetrends update to download the update.");
				Bukkit.getLogger().info("***************************************************************");
			} else if (updater.getResult() == Updater.UpdateResult.NO_UPDATE){
				//Up to date! Yay!
			}
		} else {
			Bukkit.getLogger().info("You have disabled update checking in the Minetrends configuration file!");
		}
		
		publicKey = Encryption.getServerKey();
		privateKey = Encryption.getPrivateKey();
		
		//Start TPS monitor.
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new TPSChecker(), 100L, 1L);
		
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
			conn.setConnectTimeout(4000); //set timeout
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
			Bukkit.getLogger().warning("***************************************************************");
			Bukkit.getLogger().warning("<Minetrends> Could not connect to the Minetrends servers.");
			Bukkit.getLogger().warning("***************************************************************");
		} else if (time == -1){
			//Invalid key
			Bukkit.getLogger().warning("***************************************************************");
			Bukkit.getLogger().warning("<Minetrends> You have specified an Invalid Server Key!");
			Bukkit.getLogger().warning("***************************************************************");
		} else {
			Bukkit.getLogger().info("<Minetrends> Sucessfully authenticated with Minetrends.");
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
		
		for (Player plr : playersObj){
			Map<String,String> player = new HashMap<String,String>();
			
			//Player's IP Address
			player.put("ADDRESS", Encryption.encryptString(plr.getAddress().toString()));
			
			//Player UUID. New in Minecraft v1.7
			player.put("UUID", Encryption.encryptString(plr.getUniqueId().toString()));
			
			//Player's XP Level
			player.put("XPLEVELS", Encryption.encryptString(String.valueOf(plr.getLevel())));
			
			//Add to the main data array
			playersList.put(Encryption.encryptString(plr.getName()), player);
		}
		
		data.put("players", playersList);
		
		data.put("BUKKITVERSION", Encryption.encryptString(plugin.getServer().getBukkitVersion().toString()));
		
		data.put("TIMEZONE", Encryption.encryptString(TimeZone.getDefault().getDisplayName()));
		data.put("TIMEZONEID", Encryption.encryptString(TimeZone.getDefault().getID()));
		data.put("TIMELOCAL", Encryption.encryptString(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime())));
		
		//Memory Usage
		data.put("memoryFree", "" + Encryption.encryptString(Runtime.getRuntime().freeMemory() + ""));
		data.put("memoryTotal", "" + Encryption.encryptString(Runtime.getRuntime().totalMemory() + ""));
		data.put("memoryMax", Encryption.encryptString(Runtime.getRuntime().maxMemory() + ""));
		
		//Java Virtual Machine Uptime
		long JVMStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();
		long currentTime = new Date().getTime();
		long upTime = (currentTime - JVMStartTime) / 1000;
		data.put("uptime", "" + Encryption.encryptString(upTime + ""));
		
		//TPS Monitor
		data.put("TPS", Encryption.encryptString(new DecimalFormat("#.####").format(TPSChecker.getTPS()) + ""));
		
		//Diskspace Usage
		File hd = new File("/");
		data.put("diskspaceFree", Encryption.encryptString(hd.getUsableSpace() + ""));
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
				sender.sendMessage(ChatColor.AQUA + "/minetrends key <server_key>" + ChatColor.GRAY + " Add your server key.");
				sender.sendMessage(ChatColor.AQUA + "/minetrends reload" + ChatColor.GRAY + " Reload the Minetrends configuration file.");
				sender.sendMessage(ChatColor.AQUA + "/minetrends update" + ChatColor.GRAY + " Update the plugin to the lastest version.");
				sender.sendMessage(ChatColor.AQUA + "-=-=-=-=-=-{ v" + pdfFile.getVersion() + " }-=-=-=-=-=-");
				return true;
			} else if (args.length == 1){
				if (args[0].equalsIgnoreCase("update")){
					if (sender.isOp()){
						sender.sendMessage(ChatColor.AQUA + "Updating Minetrends...");
						Updater updater = new Updater(this, 76929, this.getFile(), Updater.UpdateType.NO_VERSION_CHECK, true);
						Updater.UpdateResult result = updater.getResult();
				        switch(result) {
				            case SUCCESS:
				                // Success: The updater found an update, and has readied it to be loaded the next time the server restarts/reloads
				            	sender.sendMessage(ChatColor.AQUA + "Awesome! New Minetrends update downloaded!");
				            	sender.sendMessage(ChatColor.AQUA + "Please reload or restart the server to apply the update.");
				                break;
				            case FAIL_DOWNLOAD:
				                // Download Failed: The updater found an update, but was unable to download it.
				            	sender.sendMessage(ChatColor.RED + "Uh oh! Failed to download the new update!");
				            	sender.sendMessage(ChatColor.RED + "Please try again later or contact us if this keeps happening.");
				                break;
				            case FAIL_DBO:
				                // dev.bukkit.org Failed: For some reason, the updater was unable to contact DBO to download the file.
				            	sender.sendMessage(ChatColor.RED + "Uh oh! Failed to connect to dev.bukkit.org");
				            	sender.sendMessage(ChatColor.RED + "Please try again later or contact us if this keeps happening.");
				                break;
				            case DISABLED:
				                // Admin has globally disabled updating.
				            	sender.sendMessage(ChatColor.RED + "Uh oh! Automatic updating has been globally disabled!");
				            	sender.sendMessage(ChatColor.RED + "To use auto updating, it must be enabled in the global config. (plugins/Updater/config.yml)");
				                break;
				            case FAIL_APIKEY:
				                // The server admin has improperly configured their API key in the configuration file.
				            	sender.sendMessage(ChatColor.RED + "Uh oh! API key error.");
				            	sender.sendMessage(ChatColor.RED + "Please ensure your API key is correct. (plugins/Updater/config.yml)");
				                break;
						default:
							sender.sendMessage(ChatColor.RED + "Uh oh! Unknown Error!");
			            	sender.sendMessage(ChatColor.RED + "Please try again later or contact us if this keeps happening.");
							break;
				        }
						return true;
					} else {
						sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
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
				} else if (args[0].equalsIgnoreCase("myip")){
					if (sender.isOp()){
						sender.sendMessage(((Player) sender).getAddress().getAddress().getHostAddress());
					} else {
						return false;
					}
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
