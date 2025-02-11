package gg.umu.inventoryApi.util;

import org.bukkit.entity.Player;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SuperPlayer {
    private Player player;
    
    public SuperPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        if (player != null) {
            return player;
        } 
        return null;
    }

    private void kickPlayer(Player player, String server) {
//todo
    }

    public void sendToLobby() {
        kickPlayer(player, "lobby");
    } 
}
    