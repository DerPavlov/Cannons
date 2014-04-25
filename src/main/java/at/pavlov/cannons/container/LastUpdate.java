package at.pavlov.cannons.container;

import java.util.UUID;

public class LastUpdate {

    final UUID uid;
    long timestamp;

    public LastUpdate(UUID uid, long timestamp) {
        this.uid = uid;
        this.timestamp = timestamp;
    }

    public long getTimestamp(){
        return this.timestamp;
    }

    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }

    public UUID getUID(){
        return uid;
    }
}
