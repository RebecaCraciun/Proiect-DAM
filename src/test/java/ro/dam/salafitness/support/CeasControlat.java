package ro.dam.salafitness.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Ceas de test: "acum" este fixat si poate fi mutat inainte (ex. dupa inceperea unei sesiuni - RA04).
 */
public class CeasControlat extends Clock {

    private final ZoneId zona;
    private Instant moment;

    public CeasControlat(ZoneId zona, LocalDateTime acum) {
        this.zona = zona;
        seteaza(acum);
    }

    public void seteaza(LocalDateTime acum) {
        this.moment = acum.atZone(zona).toInstant();
    }

    public void avanseaza(Duration durata) {
        this.moment = moment.plus(durata);
    }

    @Override
    public ZoneId getZone() {
        return zona;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new CeasControlat(zone, LocalDateTime.ofInstant(moment, zone));
    }

    @Override
    public Instant instant() {
        return moment;
    }
}
