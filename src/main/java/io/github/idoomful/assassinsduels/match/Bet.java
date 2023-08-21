package io.github.idoomful.assassinsduels.match;

import io.github.idoomful.assassinscurrencycore.utils.ConfigPair;
import io.github.idoomful.assassinscurrencycore.utils.Economy;
import io.github.idoomful.assassinsduels.DMain;
import io.github.idoomful.assassinsduels.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;

public class Bet {
    private final ArrayList<ConfigPair<Integer, String>> money;
    private final UUID owner;
    private DuellerGroup group;

    public Bet(ArrayList<ConfigPair<Integer, String>> money, DuellerGroup group, UUID owner) {
        this.money = money == null ? new ArrayList<>() : money;
        this.group = group;
        this.owner = owner;
    }

    public ArrayList<ConfigPair<Integer, String>> getMoney() {
        return money;
    }

    public DuellerGroup getGroup() {
        return group;
    }

    public void setGroup(DuellerGroup group) {
        this.group = group;
    }

    public UUID getOwner() {
        return owner;
    }

    public void give(OfflinePlayer player) {
        HashMap<UUID, HashMap<String, Integer>> offline = new HashMap<>();

        for(int i = 0; i < getMoney().size(); i++) {
            UUID uuid = player.getUniqueId();
            String curr = getMoney().get(i).getValue();
            int amount = getMoney().get(i).getKey();

            if(!player.isOnline()) {
                if(!offline.containsKey(uuid)) {
                    HashMap<String, Integer> money = new HashMap<>();
                    money.put(curr, 0);
                    offline.put(uuid, money);
                }

                if(offline.get(uuid).containsKey(curr)) {
                    offline.get(uuid).put(curr, offline.get(uuid).get(curr) + amount);
                } else {
                    offline.get(uuid).put(curr, amount);
                }
            } else {
                player.getPlayer().getInventory().addItem(Economy.Currency.getMarkedItem(curr, amount));
            }

            getMoney().remove(getMoney().get(i));
        }

        getGroup().getBets().remove(getOwner());

        HashMap<UUID, ArrayList<ConfigPair<Integer, String>>> converted = new HashMap<>();

        for(Map.Entry<UUID, HashMap<String, Integer>> pair : offline.entrySet()) {
            if(!converted.containsKey(pair.getKey())) {
                converted.put(pair.getKey(), new ArrayList<>());
            }

            for(Map.Entry<String, Integer> currency : pair.getValue().entrySet()) {
                converted.get(pair.getKey()).add(new ConfigPair<>(currency.getValue(), currency.getKey()));
            }

            DMain.getInstance().getSQL().setMadeBets(Bukkit.getOfflinePlayer(pair.getKey()).getName(), converted.get(pair.getKey()));
        }
    }

    public void share(List<OfflinePlayer> players) {
        HashMap<UUID, HashMap<String, Integer>> offline = new HashMap<>();

        for(ConfigPair<Integer, String> currency : getMoney()) {
            if(players.size() == 1) {
                players.get(0).getPlayer().getInventory().addItem(Economy.Currency.getMarkedItem(currency.getValue(), currency.getKey()));
                getGroup().getBets().remove(getOwner());
            } else if(!players.isEmpty()) {
                ArrayList<ConfigPair<Integer, String>> copy = new ArrayList<>(getMoney());
                ArrayList<UUID> received = new ArrayList<>();

                for(int i = 0; i < copy.size(); i++) {
                    while(copy.get(i).getKey() > 0) {
                        double share = ((double) copy.get(i).getKey()) / players.size();
                        int intShare = (int) share;

                        if(intShare < share) {
                            if(players.stream().anyMatch(pl -> pl.getUniqueId().equals(getOwner())) && !received.contains(getOwner())) {
                                int i2 = i;
                                players.stream().filter(pl -> pl.getUniqueId().equals(getOwner())).forEach(pl -> {
                                    if(!pl.isOnline()) {
                                        UUID uuid = pl.getUniqueId();
                                        String curr = copy.get(i2).getValue();

                                        if(!offline.containsKey(uuid)) {
                                            HashMap<String, Integer> money = new HashMap<>();
                                            money.put(curr, 0);
                                            offline.put(uuid, money);
                                        }

                                        if(offline.get(uuid).containsKey(curr)) {
                                            offline.get(uuid).put(curr, offline.get(uuid).get(curr) + 1);
                                        } else {
                                            offline.get(uuid).put(curr, 1);
                                        }
                                    } else {
                                        pl.getPlayer().getInventory().addItem(Economy.Currency.getMarkedItem(copy.get(i2).getValue(), 1));
                                        copy.get(i2).setKey(copy.get(i2).getKey() - 1);
                                        received.add(getOwner());
                                    }
                                });
                            } else {
                                int random = ((int) Utils.randomRange("1-" + players.size())) - 1;

                                while(received.contains(players.get(random).getUniqueId()) && received.size() != players.size()) {
                                    random = ((int) Utils.randomRange("1-" + players.size())) - 1;
                                }

                                OfflinePlayer pl = players.get(random);
                                UUID uuid = pl.getUniqueId();
                                String curr = copy.get(i).getValue();

                                if(!pl.isOnline()) {
                                    if(!offline.containsKey(uuid)) {
                                        HashMap<String, Integer> money = new HashMap<>();
                                        money.put(curr, 0);
                                        offline.put(uuid, money);
                                    }

                                    if(offline.get(uuid).containsKey(curr)) {
                                        offline.get(uuid).put(curr, offline.get(uuid).get(curr) + 1);
                                    } else {
                                        offline.get(uuid).put(curr, 1);
                                    }
                                } else {
                                    pl.getPlayer().getInventory().addItem(Economy.Currency.getMarkedItem(curr, 1));
                                }

                                copy.get(i).setKey(copy.get(i).getKey() - 1);
                                received.add(pl.getUniqueId());
                            }
                        } else {
                            for(OfflinePlayer pl : players) {
                                UUID uuid = pl.getUniqueId();
                                String curr = copy.get(i).getValue();

                                if(!pl.isOnline()) {
                                    if(!offline.containsKey(uuid)) {
                                        HashMap<String, Integer> money = new HashMap<>();
                                        money.put(curr, 0);
                                        offline.put(uuid, money);
                                    }

                                    if(offline.get(uuid).containsKey(curr)) {
                                        offline.get(uuid).put(curr, offline.get(uuid).get(curr) + intShare);
                                    } else {
                                        offline.get(uuid).put(curr, intShare);
                                    }
                                } else {
                                    pl.getPlayer().getInventory().addItem(Economy.Currency.getMarkedItem(copy.get(i).getValue(), intShare));
                                }
                            }

                            copy.get(i).setKey(0);
                        }
                    }
                }

                getGroup().getBets().remove(getOwner());
            }
        }

        // Store the share of those offline in the DB to give them it back on join
        HashMap<UUID, ArrayList<ConfigPair<Integer, String>>> converted = new HashMap<>();

        for(Map.Entry<UUID, HashMap<String, Integer>> pair : offline.entrySet()) {
            if(!converted.containsKey(pair.getKey())) {
                converted.put(pair.getKey(), new ArrayList<>());
            }

            for(Map.Entry<String, Integer> currency : pair.getValue().entrySet()) {
                converted.get(pair.getKey()).add(new ConfigPair<>(currency.getValue(), currency.getKey()));
            }

            DMain.getInstance().getSQL().setMadeBets(Bukkit.getOfflinePlayer(pair.getKey()).getName(), converted.get(pair.getKey()));
        }
    }
}
