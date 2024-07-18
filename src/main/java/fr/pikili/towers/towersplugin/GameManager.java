package fr.pikili.towers.towersplugin;

import fr.pikili.towers.towersplugin.map.GameMap;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.Objects;

public class GameManager {

    private final JavaPlugin plugin;
    private final BoundingBox blueArea;
    private final BoundingBox redArea;
    private int timeRemainingInSeconds; // 30 minutes
    private final Scoreboard gameScoreboard;
    private final Objective towersGameObjective;
    private final Objective towersHealthObjective;
    private final String blueteam="§9Equipe bleue";
    private final String redteam="§cEquipe rouge";
    private final String gameobjectivename ="the_towers_game";
    private final String healthobjectivename ="the_towers_health";
    private Generators ironGenerator;
    private Generators emeraldGenerator;
    private Generators ironGenerator2;
    private Team blueTeam;
    private Team redTeam;
    private GameMap gameMap;


    public GameManager(JavaPlugin plugin, GameMap towersMap) {
        this.plugin = plugin;
        gameMap=towersMap;
        gameScoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        towersGameObjective = gameScoreboard.registerNewObjective(gameobjectivename, Criteria.DUMMY, "§6[The Towers]");
        towersHealthObjective = gameScoreboard.registerNewObjective(healthobjectivename, Criteria.HEALTH,"Health");

        gameScoreboard.registerNewTeam("TowersBlueTeam");
        gameScoreboard.getTeam("TowersBlueTeam").setColor(ChatColor.BLUE);
        gameScoreboard.registerNewTeam("TowersRedTeam");
        gameScoreboard.getTeam("TowersRedTeam").setColor(ChatColor.RED);

        // Démarrer les générateurs
        ironGenerator = new Generators();
        emeraldGenerator = new Generators();
        ironGenerator2 = new Generators();


        //Chargement des zones ou marquer des points
        Location blueAreaCorner1 = new Location(Bukkit.getWorld(gameMap.getWorld().getName()), plugin.getConfig().getDouble("blue_well_x_1"),
                plugin.getConfig().getDouble("blue_well_y_1"),
                plugin.getConfig().getDouble("blue_well_z_1"));
        Location blueAreaCorner2 = new Location(Bukkit.getWorld(gameMap.getWorld().getName()), plugin.getConfig().getDouble("blue_well_x_2"),
                plugin.getConfig().getDouble("blue_well_y_2"),
                plugin.getConfig().getDouble("blue_well_z_2"));
        blueArea = BoundingBox.of(blueAreaCorner1, blueAreaCorner2);

        Location redAreaCorner1 = new Location(Bukkit.getWorld(gameMap.getWorld().getName()), plugin.getConfig().getDouble("red_well_x_1"),
                plugin.getConfig().getDouble("red_well_y_1"),
                plugin.getConfig().getDouble("red_well_z_1"));
        Location redAreaCorner2 = new Location(Bukkit.getWorld(gameMap.getWorld().getName()), plugin.getConfig().getDouble("red_well_x_2"),
                plugin.getConfig().getDouble("red_well_y_2"),
                plugin.getConfig().getDouble("red_well_z_2"));
        redArea = BoundingBox.of(redAreaCorner1, redAreaCorner2);
    }

    //Tries to start the game if the number of players in each team is correct
    public void tryToStartGame() {
        blueTeam = gameScoreboard.getTeam("TowersBlueTeam");
        redTeam = gameScoreboard.getTeam("TowersRedTeam");

        int blueTeamSize = blueTeam.getEntries().size();
        int redTeamSize = redTeam.getEntries().size();

        int minPlayers = plugin.getConfig().getInt("players_required_to_start_towers"); // Nombre minimum de joueurs par équipe pour commencer

        if (blueTeamSize >= minPlayers && redTeamSize >= minPlayers) {
            if (blueTeamSize != redTeamSize) {
                String missingPlayersTeam = blueTeamSize > redTeamSize ? ChatColor.RED + "rouge" : ChatColor.BLUE + "bleue";
                int missingPlayersCount = Math.abs(blueTeamSize - redTeamSize);
                Bukkit.broadcastMessage(ChatColor.GOLD + "[The Towers] " + ChatColor.GRAY + "Il manque " + missingPlayersCount + " joueur(s) dans l'équipe " + missingPlayersTeam + ChatColor.GRAY + " pour commencer !");
                return;
            }
            startGame();
        } else {
            if (blueTeamSize < minPlayers) {
                int missingPlayersCount = minPlayers - blueTeamSize;
                Bukkit.broadcastMessage(ChatColor.GOLD + "[The Towers] " + ChatColor.GRAY + "Il manque " + missingPlayersCount + " joueur(s) dans l'équipe " + ChatColor.BLUE + "bleue" + ChatColor.GRAY + " pour commencer !");
            }
            if (redTeamSize < minPlayers) {
                int missingPlayersCount = minPlayers - redTeamSize;
                Bukkit.broadcastMessage(ChatColor.GOLD + "[The Towers] " + ChatColor.GRAY + "Il manque " + missingPlayersCount + " joueur(s) dans l'équipe " + ChatColor.RED + "rouge" + ChatColor.GRAY + " pour commencer !");
            }
        }
    }

    //Starts the game by creating all the objectives, starting a countdown, eventually teleporting all the players in their spawnpoint
    //If a team changes, it stops the countdown, and the game doesn't start.
    public void startGame() {
        blueTeam = gameScoreboard.getTeam("TowersBlueTeam");
        redTeam = gameScoreboard.getTeam("TowersRedTeam");

        blueTeam.setAllowFriendlyFire(false);
        redTeam.setAllowFriendlyFire(false);

        // Initialisation de l'objectif the_towers_game
        Score scoreToWin = towersGameObjective.getScore(ChatColor.GOLD + "Objectif : " + plugin.getConfig().getInt("points_to_win"));
        scoreToWin.setScore(15);
        Score blueScore = towersGameObjective.getScore(blueteam);
        blueScore.setScore(0);
        Score redScore = towersGameObjective.getScore(redteam);
        redScore.setScore(0);
        towersGameObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        towersHealthObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);

        new BukkitRunnable() {
            int timer = 300;

            @Override
            public void run() {
                if (timer > 0) {

                    // Check if there is always the same number of players in each team
                    if (blueTeam.getEntries().size() != redTeam.getEntries().size()) {
                        Bukkit.broadcastMessage(ChatColor.GOLD + "[The Towers] " + ChatColor.GRAY + "Annulation du lancement");
                        cancel();
                    }

                    // Check if there are players with the "TowersTeamSelection" tag who are not in a team
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getScoreboardTags().contains("TowersTeamSelection")) {
                            if (!blueTeam.hasEntry(player.getName()) && !redTeam.hasEntry(player.getName())) {
                                Bukkit.broadcastMessage(ChatColor.GOLD + "[The Towers] " + ChatColor.GRAY + "Tous les joueurs ne sont pas dans une équipe ! Reset du chrono");
                                cancel();
                                break;
                            }
                        }
                    }

                    if (timer == 300 || timer == 200 || timer == 100 || timer == 80 || timer == 60 || timer == 40 || timer == 20) {
                        displayTimer(timer / 20);
                    }

                    timer -= 20;
                } else {
                    // Stop teleportation loop
                    cancel();

                    // Starting game scheduler, which is the main scheduler
                    startGameScheduler();

                    ironGenerator.startGenerator(Bukkit.getWorld(gameMap.getWorld().getName()), 1092.5, -25., 994.5, Material.IRON_INGOT, Particle.REDSTONE, Color.GRAY);
                    emeraldGenerator.startGenerator(Bukkit.getWorld(gameMap.getWorld().getName()), 1092.5, -25., 982.5, Material.EMERALD, Particle.VILLAGER_HAPPY, Color.BLUE);
                    ironGenerator2.startGenerator(Bukkit.getWorld(gameMap.getWorld().getName()), 1092.5, -25., 970.5, Material.IRON_INGOT, Particle.REDSTONE, Color.GRAY);
                    Bukkit.getWorld(gameMap.getWorld().getName()).setGameRule(GameRule.FALL_DAMAGE, true);


                    Bukkit.getWorld(plugin.getConfig().getString("map_towers")).setDifficulty(Difficulty.NORMAL);
                    Bukkit.getWorld(plugin.getConfig().getString("map_towers")).setGameRule(GameRule.NATURAL_REGENERATION, true);

                    Bukkit.broadcastMessage(ChatColor.GOLD + "[The Towers] " + ChatColor.GRAY + "La partie commence !");
                    Objects.requireNonNull(Bukkit.getWorld(Objects.requireNonNull(plugin.getConfig().getString("map_towers")))).getPlayers().forEach(player -> {
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
                    });

                    // Teleport blue team players to their spawn
                    Location blueSpawn = new Location(Bukkit.getWorld(gameMap.getWorld().getName()),
                            plugin.getConfig().getDouble("blue_team_spawn_x"),
                            plugin.getConfig().getDouble("blue_team_spawn_y"),
                            plugin.getConfig().getDouble("blue_team_spawn_z"), 90, 0);
                    for (String entry : blueTeam.getEntries()) {
                        Player bluePlayer = Bukkit.getPlayer(entry);
                        if (bluePlayer != null && bluePlayer.isOnline()) {
                            bluePlayer.removeScoreboardTag("TowersTeamSelection");
                            bluePlayer.addScoreboardTag("TowersBlueTeam");
                            bluePlayer.addScoreboardTag("TowersGame");
                            bluePlayer.getInventory().clear();
                            bluePlayer.teleport(blueSpawn);
                            bluePlayer.setBedSpawnLocation(blueSpawn, true); // Set player's spawn point
                            giveLeatherArmor(bluePlayer, Color.BLUE); // Give player blue leather armor
                            bluePlayer.setScoreboard(gameScoreboard);
                        }
                    }

                    // Teleport red team players to their spawn
                    Location redSpawn = new Location(Bukkit.getWorld(gameMap.getWorld().getName()),
                            plugin.getConfig().getDouble("red_team_spawn_x"),
                            plugin.getConfig().getDouble("red_team_spawn_y"),
                            plugin.getConfig().getDouble("red_team_spawn_z"), -90, 0);
                    for (String entry : redTeam.getEntries()) {
                        Player redPlayer = Bukkit.getPlayer(entry);
                        if (redPlayer != null && redPlayer.isOnline()) {
                            redPlayer.removeScoreboardTag("TowersTeamSelection");
                            redPlayer.addScoreboardTag("TowersRedTeam");
                            redPlayer.addScoreboardTag("TowersGame");
                            redPlayer.getInventory().clear();
                            redPlayer.teleport(redSpawn);
                            redPlayer.setBedSpawnLocation(redSpawn, true); // Set player's spawn point
                            giveLeatherArmor(redPlayer, Color.RED); // Give player red leather armor
                            redPlayer.setScoreboard(gameScoreboard);
                        }
                    }

                    startPointsScheduler();

                }
            }
        }.runTaskTimer(plugin, 0, 20); // Execute every tick (20 times per second)


    }

    //Check if a player is in the other team's well.
    private void startPointsScheduler() {
        Team redTeam = Objects.requireNonNull(gameScoreboard.getTeam("TowersRedTeam"));
        Team blueTeam = Objects.requireNonNull(gameScoreboard.getTeam("TowersBlueTeam"));

        new BukkitRunnable() {

            @Override
            public void run() {
                for (String p : redTeam.getEntries()) {
                    Player player = Bukkit.getPlayer(p);
                    if (player != null && isPlayerInBlueArea(player)) {
                        scorePointsRed(player);
                    }
                }

                for (String p : blueTeam.getEntries()) {
                    Player player = Bukkit.getPlayer(p);
                    if (player != null && isPlayerInRedArea(player)) {
                        scorePointsBlue(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 5); // Executes every 20 ticks (1 second)
    }

    //This is the main scheduler, it contains a timer to end the game if both team doesn't score enough points
    //It starts the items generators in the middle of the map
    private void startGameScheduler() {
        timeRemainingInSeconds=30*60; //30 minutes

        ironGenerator.setGeneratorToLevelOne();
        ironGenerator2.setGeneratorToLevelOne();
        emeraldGenerator.setGeneratorToLevelOne();

        // Obtenir les joueurs des équipes blueTeam et redTeam
        ArrayList<Player> blueTeamPlayers = new ArrayList<>();
        ArrayList<Player> redTeamPlayers = new ArrayList<>();

        if (blueTeam != null) {
            for (String entry : blueTeam.getEntries()) {
                Player player = Bukkit.getPlayer(entry);
                if (player != null) {
                    blueTeamPlayers.add(player);
                }
            }
        }

        if (redTeam != null) {
            for (String entry : redTeam.getEntries()) {
                Player player = Bukkit.getPlayer(entry);
                if (player != null) {
                    redTeamPlayers.add(player);
                }
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                timeRemainingInSeconds--;
                Bukkit.getConsoleSender().sendMessage(timeRemainingInSeconds+"s");

                //Check if a player falls into the void
                for (Player player : blueTeamPlayers) {
                    if (player.getLocation().getY() < -70) {
                        player.setHealth(0.0D);
                    }
                }

                for (Player player : redTeamPlayers) {
                    if (player.getLocation().getY() < -70) {
                        player.setHealth(0.0D);
                    }
                }


                // Broadcast messages and adjust generator delays at specific times
                if (timeRemainingInSeconds == 15 * 60) {
                    Bukkit.broadcastMessage(ChatColor.GOLD + "[The Towers]" + ChatColor.GRAY + " 15 minutes se sont écoulées, les générateurs accélèrent !");
                    ironGenerator.setGeneratorToLevelTwo();
                    ironGenerator2.setGeneratorToLevelTwo();
                    emeraldGenerator.setGeneratorToLevelTwo();
                }

                if (timeRemainingInSeconds == 10 * 60) {
                    Bukkit.broadcastMessage(ChatColor.GOLD + "[The Towers]" + ChatColor.GRAY + " 20 minutes se sont écoulées, les générateurs accélèrent encore !");
                    ironGenerator.setGeneratorToLevelThree();
                    ironGenerator2.setGeneratorToLevelThree();
                    emeraldGenerator.setGeneratorToLevelThree();
                }

                if (timeRemainingInSeconds == 5 * 60) {
                    String message=ChatColor.GOLD + "[The Towers]" + ChatColor.GRAY + " 25 minutes se sont écoulées !\n";
                    message+= ChatColor.YELLOW.toString() + ChatColor.MAGIC + "!" +
                            ChatColor.AQUA + ChatColor.MAGIC + "!!!" +
                            ChatColor.LIGHT_PURPLE + ChatColor.MAGIC + "!!!" +
                            ChatColor.DARK_BLUE + " Générateurs en folie !! " +
                            ChatColor.YELLOW +  ChatColor.MAGIC + "!" +
                            ChatColor.AQUA +  ChatColor.MAGIC + "!!!" +
                            ChatColor.LIGHT_PURPLE +  ChatColor.MAGIC + "!!!" +
                            ChatColor.DARK_PURPLE +  ChatColor.MAGIC + " ";

                    Bukkit.broadcastMessage(message);
                    ironGenerator.setGeneratorToLevelFour();
                    ironGenerator2.setGeneratorToLevelFour();
                    emeraldGenerator.setGeneratorToLevelFour();
                }

                if (timeRemainingInSeconds <= 0) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stoptowers");
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20); // Run every second (20 ticks)
    }

    //Resets the map, displays the result of the game
    public void stopGame(){
        World world = Bukkit.getWorld(Objects.requireNonNull(plugin.getConfig().getString("map_towers")));

        assert world != null;
        world.setGameRule(GameRule.FALL_DAMAGE, false);

        //Stopper les particules
        ironGenerator.stopGenerators(world, 1092.5, -25., 994.5);
        ironGenerator2.stopGenerators(world, 1092.5, -25., 982.5);
        emeraldGenerator.stopGenerators(world, 1092.5, -25., 970.5);

        Team blueTeam = gameScoreboard.getTeam("TowersBlueTeam");
        Team redTeam = gameScoreboard.getTeam("TowersRedTeam");

        if (blueTeam == null || redTeam == null) {
            Bukkit.broadcastMessage(ChatColor.RED + "L'équipe rouge ou bleue n'existe pas ou n'a pas été correctement configurée !");
            return;
        }

        Score scoreBlueTeam = Objects.requireNonNull(gameScoreboard.getObjective(gameobjectivename)).getScore(blueteam);
        Score scoreRedTeam = Objects.requireNonNull(gameScoreboard.getObjective(gameobjectivename)).getScore(redteam);

        String winners="";

        // Announce the end of the game with the winning team
        if (scoreBlueTeam.getScore()<scoreRedTeam.getScore()) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "[The Towers] " + ChatColor.RED + "L'équipe rouge a gagné la partie !");
            winners = "RED";
        } else if (scoreBlueTeam.getScore()>scoreRedTeam.getScore()) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "[The Towers] " + ChatColor.BLUE + "L'équipe bleue a gagné la partie !");
            winners = "BLUE";
        } else {
            Bukkit.broadcastMessage(ChatColor.GOLD + "[The Towers] " + ChatColor.GRAY + "La partie est terminée !");
        }




        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
        }

        displayResults(winners);

        stopPointsScheduler();

        //Little delay before teleporting to the lobby
        new BukkitRunnable() {
            int count = 200; // 10 seconds * 20 ticks per second

            @Override
            public void run() {
                count -= 1; // Reduce the count by the interval

                if (count <= 0) {
                    cancel();

                    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "La partie a été arrêtée avec succès.");

                    Location lobby = new Location(Bukkit.getWorld("world"), 244.5, 52., 1348.5, 90, 0);
                    getToLobby(blueTeam, lobby);
                    getToLobby(redTeam, lobby);

                    // Remove all players from teams + clear their inventory
                    clearPlayersInventory(blueTeam);
                    clearPlayersInventory(redTeam);
                    clearTeams(blueTeam);
                    clearTeams(redTeam);

                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "reloadtowers");
                }
            }
        }.runTaskTimer(plugin, 0, 1);


        //Delete the scoreboard
        resetObjective();
    }

    private void stopPointsScheduler() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }

    private void scorePointsBlue(Player player) {
        Bukkit.broadcastMessage(ChatColor.GOLD + "[The Towers] " + ChatColor.BLUE + player.getName() + ChatColor.GRAY + " a marqué un point !");
        player.teleport(new Location(Bukkit.getWorld(gameMap.getWorld().getName()), plugin.getConfig().getDouble("blue_team_spawn_x"),
                plugin.getConfig().getDouble("blue_team_spawn_y"),
                plugin.getConfig().getDouble("blue_team_spawn_z"), 90, 0));
        Objective objective = gameScoreboard.getObjective(gameobjectivename);
        if (objective != null) {
            Score score = objective.getScore(blueteam);
            score.setScore(score.getScore() + 1);
            Bukkit.getWorld(Objects.requireNonNull(plugin.getConfig().getString("map_towers"))).getPlayers().forEach(p -> {
                p.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 0.75F);
            });

            if (score.getScore() >= plugin.getConfig().getInt("points_to_win")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stoptowers BLUE");
            }
        }
    }

    private void scorePointsRed(Player player) {
        Bukkit.broadcastMessage(ChatColor.GOLD + "[The Towers] " + ChatColor.RED + player.getName() + ChatColor.GRAY + " a marqué un point !");
        player.teleport(new Location(Bukkit.getWorld(gameMap.getWorld().getName()), plugin.getConfig().getDouble("red_team_spawn_x"),
                plugin.getConfig().getDouble("red_team_spawn_y"),
                plugin.getConfig().getDouble("red_team_spawn_z"), -90, 0));
        Objective objective = gameScoreboard.getObjective(gameobjectivename);
        if (objective != null) {
            Score score = objective.getScore(redteam);
            score.setScore(score.getScore() + 1);
            Bukkit.getWorld(Objects.requireNonNull(plugin.getConfig().getString("map_towers"))).getPlayers().forEach(p -> {
                p.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 0.75F);
            });

            if (score.getScore() >= plugin.getConfig().getInt("points_to_win")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stoptowers RED");
            }
        }
    }


    //Utilities

    private boolean isPlayerInRedArea(Player player) {
        return redArea.contains(player.getLocation().toVector());
    }

    private boolean isPlayerInBlueArea(Player player) {
        return blueArea.contains(player.getLocation().toVector());
    }

    // Méthode pour donner une armure en cuir bleue à un joueur
    private void giveLeatherArmor(Player player, Color color) {
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);

        LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();
        meta.setColor(color);
        helmet.setItemMeta(meta);
        chestplate.setItemMeta(meta);
        leggings.setItemMeta(meta);
        boots.setItemMeta(meta);

        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);
    }

    private void displayTimer(int time) {
        Bukkit.broadcastMessage(ChatColor.GOLD + "[The Towers] " + ChatColor.GRAY + "La partie commence dans " + time + " secondes !");
        Bukkit.getWorld(Objects.requireNonNull(plugin.getConfig().getString("map_towers"))).getPlayers().forEach(player -> {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1F, 1F);
        });
    }

    // Function to teleport team players to the lobby and clear their inventory
    private void getToLobby(Team team, Location lobby) {
        teleportPlayers(team, lobby, 90);
    }

    // Method to teleport team players to a given location
    private void teleportPlayers(Team team, Location location, float yaw) {
        for (String entry : team.getEntries()) {
            Player teamPlayer = Bukkit.getPlayer(entry);
            if (teamPlayer != null && teamPlayer.isOnline()) {
                teamPlayer.teleport(location);
                teamPlayer.setBedSpawnLocation(location, true); // Set player's spawn point
            }
        }
    }

    // Method to clear the inventory of all team players
    private void clearPlayersInventory(Team team) {
        for (String entry : team.getEntries()) {
            Player teamPlayer = Bukkit.getPlayer(entry);
            if (teamPlayer != null && teamPlayer.isOnline()) {
                teamPlayer.getInventory().clear();
                teamPlayer.setTotalExperience(0);
            }
        }
    }

    // Method to remove all players from a team
    private void clearTeams(Team team) {
        for (String entry : team.getEntries()) {
            Player player = Bukkit.getPlayer(entry);
            if (player != null) {
                player.removeScoreboardTag("TowersBlueTeam");
                player.removeScoreboardTag("TowersRedTeam");
                player.removeScoreboardTag("TowersGame");
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
            team.removeEntry(entry);
        }
    }

    private void displayResults(String winners) {
        Bukkit.broadcastMessage(ChatColor.BOLD+"--------------------------------------");
        Bukkit.broadcastMessage("                +" + ChatColor.BOLD + "The Towers");

        if (winners.equals("RED")) {
            Bukkit.broadcastMessage("|  L'équipe rouge emporte la partie !  |");
        } else if (winners.equals("BLUE")) {
            Bukkit.broadcastMessage("|  L'équipe bleue emporte la partie !  |");
        } else {
            Bukkit.broadcastMessage("|  PAS DE GAGNANTSSSSSSSSSSSSSSSSSS !  |");
        }

        Bukkit.broadcastMessage(ChatColor.BOLD+"--------------------------------------");
    }

    public void resetObjective(){
        Score blueTeamScore = gameScoreboard.getObjective(gameobjectivename).getScore(blueteam);
        blueTeamScore.setScore(0);

        Score redTeamScore = gameScoreboard.getObjective(gameobjectivename).getScore(redteam);
        redTeamScore.setScore(0);

        gameScoreboard.clearSlot(DisplaySlot.SIDEBAR);
        gameScoreboard.clearSlot(DisplaySlot.PLAYER_LIST);
    }

    public Scoreboard getTowersScoreboard(){
        return gameScoreboard;
    }
}
