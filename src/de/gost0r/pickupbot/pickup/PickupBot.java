package de.gost0r.pickupbot.pickup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import de.gost0r.pickupbot.discord.DiscordBot;
import de.gost0r.pickupbot.discord.DiscordChannel;
import de.gost0r.pickupbot.discord.DiscordChannelType;
import de.gost0r.pickupbot.discord.DiscordMessage;
import de.gost0r.pickupbot.discord.DiscordRole;
import de.gost0r.pickupbot.discord.DiscordUser;

public class PickupBot extends DiscordBot {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private DiscordChannel latestMessageChannel;
	
	public PickupLogic logic;
	
	@Override
	public void init() {
		super.init();
		
//		pubchan = DiscordChannel.findChannel("143233743107129344"); // pickup_dev
//		pubchan = DiscordChannel.findChannel("402541587164561419"); // urtpickup
		
		logic = new PickupLogic(this);
//		logic.cmdEnableGametype("TEST", "1");
		
		// TEST
//		Player p = Player.get("v3nd3tta");
//		Gametype gt = logic.getGametypeByString("test");
//		List<Gametype> list = new ArrayList<Gametype>();
//		list.add(gt);
//		logic.cmdAddPlayer(p, list);
	}
	
	@Override
	protected void tick() {
		super.tick();
		
		if (logic != null) {
			logic.afkCheck();
		}
			
		
	}

	@Override
	protected void recvMessage(DiscordMessage msg) {
		LOGGER.info("RECV #" + ((msg.channel == null || msg.channel.name == null) ?  "null" : msg.channel.name) + " " + msg.user.username + ": " + msg.content);
		
		this.latestMessageChannel = msg.channel;
		
		if (msg.user.equals(self)) {
//			System.out.println("Msg from self, ignore.");
			return;
		}
		
		String[] data = msg.content.split(" ");

		if (isChannel(PickupChannelType.PUBLIC, msg.channel))
		{
			Player p = Player.get(msg.user);
			
			// AFK CHECK CODE
			if (p != null) {
				p.afkCheck();
			}
			
			// Execute code according to cmd
			switch (data[0].toLowerCase()) 
			{
				case Config.CMD_ADD:
					if (data.length > 1)
					{
						if (p != null)
						{
							List<Gametype> gametypes = new ArrayList<Gametype>();
							String[] modes = Arrays.copyOfRange(data, 1, data.length);
							for (String mode : modes) {
								Gametype gt = logic.getGametypeByString(mode);
								if (gt != null) {
									gametypes.add(gt);
								}
							}
							if (gametypes.size() > 0) {
								logic.cmdAddPlayer(p, gametypes);
							} else {
								sendNotice(msg.user, Config.no_gt_found);
							}
						}
						else sendNotice(msg.user, Config.user_not_registered);
					}
					else sendNotice(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_ADD));
					break;
					
				case Config.CMD_REMOVE:
					Player player = p;
					int startindex = 1;
					if (hasAdminRights(msg.user) && data.length > 1)
					{
						DiscordUser u = DiscordUser.getUser(data[1].replaceAll("[^\\d.]", ""));
						if (u != null)
						{
							player = Player.get(u);
							if (data.length > 2)
							{
								startindex = 2;
							}
							else
							{
								startindex = -1;
							}
						}
					}

					List<Gametype> gametypes = new ArrayList<Gametype>();
					if (data.length == 1 || startindex == -1) 
					{
						gametypes = null;
					}
					else
					{
						String[] modes = Arrays.copyOfRange(data, startindex, data.length);
						for (String mode : modes) {
							Gametype gt = logic.getGametypeByString(mode);
							if (gt != null) {
								gametypes.add(gt);
							}
						}
					}
					logic.cmdRemovePlayer(player, gametypes);
					break;
					
				case Config.CMD_MAPS:
				case Config.CMD_MAP:
					if (p != null)
					{
						if (data.length == 1)
						{
							logic.cmdGetMaps();
						}
						else if (data.length == 2)
						{
							logic.cmdMapVote(p, null, data[1]);
						}
						else if (data.length == 3)
						{
							Gametype gt = logic.getGametypeByString(data[1]);
							logic.cmdMapVote(p, gt, data[2]);
						}
						else sendNotice(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_MAP));
					}
					else sendNotice(msg.user, Config.user_not_registered);
					break;
					
				case Config.CMD_STATUS:
					if (data.length == 1)
					{
						logic.cmdStatus();
					}
					else sendNotice(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_STATUS));
					break;
					
				case Config.CMD_SURRENDER:
					if (data.length == 1)
					{
						if (p != null)
						{
							logic.cmdSurrender(p);
						}
					}
					else sendNotice(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_SURRENDER));
					break;
					
				case Config.CMD_RESET:
					if (hasAdminRights(msg.user))
					{
						if (data.length == 1)
						{
							logic.cmdReset("all");
						}
						else if (data.length == 2)
						{
							logic.cmdReset(data[1]);
						}
						else sendNotice(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_RESET));
					}
					break;
				
				case Config.CMD_LOCK:
					if (hasAdminRights(msg.user))
					{
						if (data.length == 1)
						{
							logic.cmdLock();
						}
						else super.sendMsg(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_LOCK));
					}
					break;
					
				case Config.CMD_UNLOCK:
					if (hasAdminRights(msg.user))
					{
						if (data.length == 1)
						{
							logic.cmdUnlock();
						}
						else super.sendMsg(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_UNLOCK));
					}
					break;
					
				case Config.CMD_GETELO:
					if (p != null)
					{
						if (data.length == 1)
						{
							logic.cmdGetElo(p);
						}
						else if (data.length == 2)
						{
							Player pOther = null;
							DiscordUser u = DiscordUser.getUser(data[1].replaceAll("[^\\d.]", ""));
							if (u != null)
							{
								pOther = Player.get(u);
							}
							else
							{
								pOther = Player.get(data[1].toLowerCase());
							}
							
							if (pOther != null)
							{
								logic.cmdGetElo(pOther);
							}
							else sendNotice(msg.user, Config.player_not_found);
						}
						else sendNotice(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_GETELO));	
					}
					else sendNotice(msg.user, Config.user_not_registered);
					break;
					
				case Config.CMD_TOP5: 
					if (p != null)
					{
						if (data.length == 1)
						{
							logic.cmdTopElo(5);
						}
						else sendNotice(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_TOP5));
					}
					else sendNotice(msg.user, Config.user_not_registered);
					break;

				case Config.CMD_REGISTER:
					if (data.length == 2)
					{
						logic.cmdRegisterPlayer(msg.user, data[1].toLowerCase(), msg.id);
					}
					else sendNotice(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_REGISTER));
					break;
					
				case Config.CMD_LIVE:
					if (p != null)
					{
						if (data.length == 1)
						{
							logic.cmdLive();
						}
						else sendNotice(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_LIVE));
					}
					else sendNotice(msg.user, Config.user_not_registered);
					break;
					
				case Config.CMD_MATCH:
					if (p != null)
					{
						if (data.length == 2)
						{
							logic.cmdDisplayMatch(data[1]);
						}
						else sendNotice(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_MATCH));
					}
					else sendNotice(msg.user, Config.user_not_registered);
					break;
					
				case Config.CMD_BANINFO:
					if (p != null)
					{
						if (data.length == 1)
						{
							sendMsg(msg.channel, logic.printBanInfo(p));
						}
						else if (data.length == 2)
						{
							Player pOther = null;
							DiscordUser u = DiscordUser.getUser(data[1].replaceAll("[^\\d.]", ""));
							if (u != null)
							{
								pOther = Player.get(u);
							}
							else
							{
								pOther = Player.get(data[1].toLowerCase());
							}
							
							if (pOther != null)
							{
								sendMsg(msg.channel, logic.printBanInfo(pOther));
							}
							else sendNotice(msg.user, Config.player_not_found);
						}
						else sendNotice(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_BANINFO));	
					}
					else sendNotice(msg.user, Config.user_not_registered);
					break;
			}
		}

		if (isChannel(PickupChannelType.ADMIN, msg.channel) || msg.channel.type == DiscordChannelType.DM)
		{
			if (hasAdminRights(msg.user))
			{
				// Execute code according to cmd
				switch (data[0].toLowerCase())
				{
					case Config.CMD_GETDATA:
						if (data.length == 2)
						{
							logic.cmdGetData(msg.user, data[1]);
						}
						else super.sendMsg(msg.channel, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_GETDATA));
						break;
						
					case Config.CMD_ENABLEMAP:
						if (data.length == 3)
						{
							if (logic.cmdEnableMap(data[1], data[2]))
							{
								super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
						}
						else super.sendMsg(msg.channel, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_ENABLEMAP));
						break;
						
					case Config.CMD_DISABLEMAP:
						if (data.length == 3)
						{
							if (logic.cmdDisableMap(data[1], data[2]))
							{
								super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
						}
						else super.sendMsg(msg.channel, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_DISABLEMAP));
						break;
						
					case Config.CMD_ENABLEGAMETYPE:
						if (data.length == 3)
						{
							if (logic.cmdEnableGametype(data[1], data[2]))
							{
								super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
						}
						else super.sendMsg(msg.channel, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_ENABLEGAMETYPE));	
						break;
						
					case Config.CMD_DISABLEGAMETYPE:
						if (data.length == 2)
						{
							if (logic.cmdDisableGametype(data[1]))
							{
								super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
						}
						else super.sendMsg(msg.channel, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_ENABLEGAMETYPE));
						break;
						
					case Config.CMD_ADDGAMECONFIG:
						if (data.length >= 3)
						{
							if (logic.cmdAddGameConfig(data[1], msg.content.substring(Config.CMD_ADDGAMECONFIG.length() + data[1].length() + 2)))
							{
								super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
						}
						else super.sendMsg(msg.channel, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_ADDGAMECONFIG));
						break;
						
					case Config.CMD_REMOVEGAMECONFIG:
						if (data.length >= 3)
						{
							if (logic.cmdRemoveGameConfig(data[1], msg.content.substring(Config.CMD_REMOVEGAMECONFIG.length() + data[1].length() + 2)))
							{
								super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
						}
						else super.sendMsg(msg.channel, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_REMOVEGAMECONFIG));
						break;
						
					case Config.CMD_LISTGAMECONFIG:
						if (data.length == 2)
						{
							if (logic.cmdListGameConfig(msg.channel, data[1]))
							{
								// if successful it will print the info
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
						}
						else super.sendMsg(msg.channel, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_LISTGAMECONFIG));
						break;
						
					case Config.CMD_ADDSERVER:
						if (data.length == 3)
						{
							if (logic.cmdAddServer(data[1], data[2]))
							{
								super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
						}
						else super.sendMsg(msg.channel, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_ADDSERVER));
						break;
						
					case Config.CMD_ENABLESERVER:
						if (data.length == 2)
						{
							if (logic.cmdServerActivation(data[1], true))
							{
								super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
						}
						else super.sendMsg(msg.channel, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_ENABLESERVER));
						break;
						
					case Config.CMD_DISABLESERVER:
						if (data.length == 2)
						{
							if (logic.cmdServerActivation(data[1], false))
							{
								super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
						}
						else super.sendMsg(msg.channel, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_DISABLESERVER));
						break;
						
					case Config.CMD_UPDATESERVER:
						if (data.length == 3)
						{
							if (logic.cmdServerChangeRcon(data[1], data[2]))
							{
								super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
						}
						else super.sendMsg(msg.channel, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_UPDATESERVER));
						break;
						
					case Config.CMD_SHOWSERVERS:
						if (data.length == 1)
						{
							logic.cmdServerList(msg.channel);
						}
						else super.sendMsg(msg.channel, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_SHOWSERVERS));
						break;
					
					case Config.CMD_RCON:
						if (data.length > 2)
						{
							if (logic.cmdServerSendRcon(data[1], msg.content.substring(Config.CMD_RCON.length() + data[1].length() + 2)))
							{
								super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
						}
						else super.sendMsg(msg.channel, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_RCON));
						break;
						
					case Config.CMD_SHOWMATCHES:
						if (data.length == 1)
						{
							logic.cmdMatchList(msg.channel);
						}
						else super.sendMsg(msg.channel, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_SHOWMATCHES));
						break;
						
					case Config.CMD_UNREGISTER:
						if (data.length == 2)
						{
							Player player = Player.get(data[1].toLowerCase());
							if (player != null)
							{
								if (logic.cmdUnregisterPlayer(player))
								{
									super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
								}
								else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
							}
							else sendMsg(msg.channel, Config.player_not_found);
						}
						else super.sendMsg(msg.channel, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_UNREGISTER));
						break;		
				}
			}
		}
		
		if (isChannel(PickupChannelType.PUBLIC, msg.channel)
				|| isChannel(PickupChannelType.ADMIN, msg.channel)
				|| msg.channel.type == DiscordChannelType.DM)
		{
			if (data[0].toLowerCase().equals(Config.CMD_HELP))
			{
				if (data.length == 2)
				{
					String cmd = (!data[1].startsWith("!") ? "!" : "") + data[1];
					switch (cmd)
					{
						case Config.CMD_ADD: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_ADD)); break;
						case Config.CMD_REMOVE: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_REMOVE)); break;
						case Config.CMD_MAPS: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_MAPS)); break;
						case Config.CMD_MAP: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_MAP)); break;
						case Config.CMD_STATUS: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_STATUS)); break;
						case Config.CMD_HELP: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_HELP)); break;
						case Config.CMD_LOCK: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_LOCK)); break;
						case Config.CMD_UNLOCK: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_UNLOCK)); break;
						case Config.CMD_RESET: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_RESET)); break;
						case Config.CMD_GETDATA: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_GETDATA)); break;
						case Config.CMD_ENABLEMAP: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_ENABLEMAP)); break;
						case Config.CMD_DISABLEMAP: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_DISABLEMAP)); break;
						case Config.CMD_RCON: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_RCON)); break;
						case Config.CMD_REGISTER: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_REGISTER)); break;
						case Config.CMD_GETELO: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_GETELO)); break;
						case Config.CMD_TOP5: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_TOP5)); break;
						case Config.CMD_MATCH: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_MATCH)); break;
						case Config.CMD_SURRENDER: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_SURRENDER)); break;
						case Config.CMD_LIVE: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_LIVE)); break;
						//case Config.CMD_REPORT: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_REPORT)); break;
						//case Config.CMD_EXCUSE: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_EXCUSE)); break;
						//case Config.CMD_REPORTLIST: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_REPORTLIST)); break;
						//case Config.CMD_ADDBAN: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_ADDBAN)); break;
						//case Config.CMD_REMOVEBAN: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_REMOVEBAN)); break;
						case Config.CMD_BANINFO: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_BANINFO)); break;
						case Config.CMD_SHOWSERVERS: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_SHOWSERVERS)); break;
						case Config.CMD_ADDSERVER: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_ADDSERVER)); break;
						case Config.CMD_ENABLESERVER: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_ENABLESERVER)); break;
						case Config.CMD_DISABLESERVER: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_DISABLESERVER)); break;
						case Config.CMD_UPDATESERVER: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_UPDATESERVER)); break;
						case Config.CMD_ENABLEGAMETYPE: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_ENABLEGAMETYPE)); break;
						case Config.CMD_DISABLEGAMETYPE: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_DISABLEGAMETYPE)); break;
						case Config.CMD_SHOWMATCHES: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_SHOWMATCHES)); break;
						case Config.CMD_ADDGAMECONFIG: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_ADDGAMECONFIG)); break;
						case Config.CMD_REMOVEGAMECONFIG: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_REMOVEGAMECONFIG)); break;
						case Config.CMD_LISTGAMECONFIG: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_LISTGAMECONFIG)); break;
						default: super.sendMsg(msg.channel, Config.help_unknown); break;
					}
				}
				else
				{
					if (isChannel(PickupChannelType.PUBLIC, msg.channel))
					{
						super.sendMsg(msg.channel, Config.help_cmd_avi.replace(".cmds.", Config.PUB_LIST));
					}
					if (hasAdminRights(msg.user) && (isChannel(PickupChannelType.ADMIN, msg.channel) || msg.channel.type == DiscordChannelType.DM))
					{
						super.sendMsg(msg.channel, Config.help_cmd_avi.replace(".cmds.", Config.ADMIN_LIST));
					}
				}
			}
			else if (data[0].toLowerCase().equals(Config.CMD_ADDCHANNEL))
			{
				if (hasAdminRights(msg.user))
				{
					if (data.length == 3)
					{
						DiscordChannel targetChannel = DiscordChannel.findChannel(data[1].replaceAll("[^\\d.]", ""));
						if (targetChannel != null)
						{
							try {
								PickupChannelType type = PickupChannelType.valueOf(data[2].toUpperCase());
								if (!logic.getChannelByType(type).contains(targetChannel)) 
								{
									if (logic.addChannel(type, targetChannel))
									{
										sendNotice(msg.user, "successfully added the channel");
									}
									else sendNotice(msg.user, "unsuccessfully added the channel");
								}
					        }
							catch (IllegalArgumentException e)
							{
								sendNotice(msg.user, "unknown channel type");
					        }
						}
						else sendNotice(msg.user, "invalid channel");
					}
					else sendNotice(msg.user, "invalid options");
				}
			}
			else if (data[0].toLowerCase().equals(Config.CMD_REMOVECHANNEL))
			{
				if (hasAdminRights(msg.user))
				{
					if (data.length == 3)
					{
						DiscordChannel targetChannel = DiscordChannel.findChannel(data[1].replaceAll("[^\\d.]", ""));
						if (targetChannel != null)
						{
							try
							{
								PickupChannelType type = PickupChannelType.valueOf(data[2].toUpperCase());
								if (logic.getChannelByType(type).contains(targetChannel))
								{
									if (logic.removeChannel(type, targetChannel))
									{
										sendNotice(msg.user, "successfully removed the channel.");
									}
									else sendNotice(msg.user, "unsuccessfully removed the channel.");
								}

					        }
							catch (IllegalArgumentException e)
							{
								sendNotice(msg.user, "unknown role type");
					        }
						}
						else sendNotice(msg.user, "invalid channel");
					}
					else sendNotice(msg.user, "invalid options");
				}
			}
			else if (data[0].toLowerCase().equals(Config.CMD_ADDROLE))
			{
				if (hasSuperAdminRights(msg.user))
				{
					if (data.length == 3)
					{
						DiscordRole targetRole = DiscordRole.getRole(data[1].replaceAll("[^\\d.]", ""));
						if (targetRole != null)
						{
							try
							{
								PickupRoleType type = PickupRoleType.valueOf(data[2].toUpperCase());
								if (!logic.getRoleByType(type).contains(targetRole)) 
								{
									if (logic.addRole(type, targetRole))
									{
										sendNotice(msg.user, "successfully added the role.");
									}
									else sendNotice(msg.user, "unsuccessfully removed the role.");
								}

					        }
							catch (IllegalArgumentException e)
							{
								sendNotice(msg.user, "unknown role type");
					        }
						}
						else sendNotice(msg.user, "invalid channel");
					}
					else sendNotice(msg.user, "invalid options");
				}
			}
			else if (data[0].toLowerCase().equals(Config.CMD_REMOVEROLE))
			{
				if (hasSuperAdminRights(msg.user))
				{
					if (data.length == 3)
					{
						DiscordRole targetRole = DiscordRole.getRole(data[1].replaceAll("[^\\d.]", ""));
						if (targetRole != null)
						{
							try
							{
								PickupRoleType type = PickupRoleType.valueOf(data[2].toUpperCase());
								if (logic.getRoleByType(type).contains(targetRole)) 
								{
									if (logic.removeRole(type, targetRole))
									{
										sendNotice(msg.user, "successfully removed the role.");
									}
									else sendNotice(msg.user, "unsuccessfully removed the role.");
								}
							}
							catch (IllegalArgumentException e)
							{
								sendNotice(msg.user, "unknown role type");
					        }
						}
						else sendNotice(msg.user, "invalid role");
					}
					else sendNotice(msg.user, "invalid options");
				}
			}
				
		}
		
		if (data[0].toLowerCase().equals("!showroles"))
		{
			DiscordUser u = msg.user;
			if (data.length == 2)
			{
				DiscordUser testUser = super.parseMention(data[1]);
				if (testUser != null)
				{
					u = testUser;
				}
			}
			List<DiscordRole> list = u.getRoles(DiscordBot.getGuild());
			String message = "";
			for (DiscordRole role : list)
			{
				message += role.getMentionString() + " ";
			}
			sendNotice(u, message);
		}
		
		if (data[0].toLowerCase().equals("!showknownroles"))
		{
			String message = "Roles: ";
			for (PickupRoleType type : logic.getRoleTypes()) {
				message += "\n**" + type.name() + "**:";

				for (DiscordRole role : logic.getRoleByType(type))
				{
					message += " " + role.getMentionString() + " ";
				}
			}
			sendMsg(msg.channel, message);
		}
		
		if (data[0].toLowerCase().equals("!showknownchannels"))
		{
			String message = "Channels: ";
			for (PickupChannelType type : logic.getChannelTypes()) {
				message += "\n**" + type.name() + "**:";

				for (DiscordChannel channel : logic.getChannelByType(type))
				{
					message += " " + channel.getMentionString() + " ";
				}
			}
			sendMsg(msg.channel, message);
		}
		
		if (data[0].toLowerCase().equals("!godrole")) {
			if (logic.getRoleByType(PickupRoleType.SUPERADMIN).size() == 0) {
				if (data.length == 2) {
					DiscordRole role = DiscordRole.getRole(data[1].replaceAll("[^\\d.]", ""));
					if (role != null) {
						logic.addRole(PickupRoleType.SUPERADMIN, role);
						sendNotice(msg.user, "*" + role.getMentionString() + " set as SUPERADMIN role*");
					}
				}
			}
		}
		
		if (data[0].toLowerCase().equals("!banme")) {
			//Player p = Player.get(msg.user);
			//logic.banPlayer(p, BanReason.NOSHOW);
		}
	}
	
	public boolean isChannel(PickupChannelType type, DiscordChannel channel) {
		return logic.getChannelByType(type).contains(channel);
	}
		
	public boolean hasAdminRights(DiscordUser user) {
		List<DiscordRole> roleList = user.getRoles(DiscordBot.getGuild());
		List<DiscordRole> adminList = logic.getAdminList();
		for (DiscordRole s : roleList) {
			for (DiscordRole r : adminList) {
				if (s.equals(r)) {
					return true;
				}
			}
		}
		return false;
	}
	
public boolean hasSuperAdminRights(DiscordUser user) {
	List<DiscordRole> roleList = user.getRoles(DiscordBot.getGuild());
	List<DiscordRole> adminList = logic.getSuperAdminList();
	for (DiscordRole s : roleList) {
		for (DiscordRole r : adminList) {
			if (s.equals(r)) {
				return true;
			}
		}
	}
	return false;
}


	public void sendNotice(DiscordUser user, String msg) {
		sendMsg(getLatestMessageChannel(), user.getMentionString() + " " + msg);
	}

	public void sendMsg(List<DiscordChannel> channelList, String msg) {
		for (DiscordChannel channel : channelList) {
			sendMsg(channel, msg);
		}
	}
	
	public DiscordChannel getLatestMessageChannel() {
		return latestMessageChannel;
	}
}