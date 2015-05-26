package at.pavlov.cannons.container;

import org.bukkit.Sound;

import java.util.Locale;
import java.util.Scanner;

public class SoundHolder {
    private Sound sound;
    private Float volume;
    private Float pitch;

    public SoundHolder(String str)
    {
        // data structure:
        // 'IRONGOLEM_WALK:1:0.5'
        try
        {
            sound = null;
            volume = 1.0F;
            pitch = 1.0F;
            Scanner s = new Scanner(str).useDelimiter("\\s*:\\s*");

            // use US locale to be able to identify floats in the string
            s.useLocale(Locale.US);

            if (s.hasNext())
            {
                String scan = s.next();
                if (scan!=null && !scan.equalsIgnoreCase("none"))
                    sound = Sound.valueOf(scan);
            }
            else
                System.out.println("missing sound value in: " + str);

            if (s.hasNextFloat())
                volume = s.nextFloat();
            else
                System.out.println("missing volume value in: " + str);
            if (s.hasNextFloat())
                pitch = s.nextFloat();
            else
                System.out.println("missing pitch value in: " + str);
            s.close();
        }
        catch(Exception e)
        {
            System.out.println("Error while converting " + str + ". Formatting: 'IRONGOLEM_WALK:1:0.5'" + e.toString());
        }
    }

    public SoundHolder(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public Sound getSound() {
        return sound;
    }

    public void setSound(Sound sound) {
        this.sound = sound;
    }

    public Float getVolume() {
        return volume;
    }

    public void setVolume(Float volume) {
        this.volume = volume;
    }

    public Float getPitch() {
        return pitch;
    }

    public void setPitch(Float pitch) {
        this.pitch = pitch;
    }

    public boolean isValid(){
        return this.sound!=null;
    }
}
