package br.com.viniciusfinancas.financas.util;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static String formatToBrasiliaTime(Instant instant) {
        // Converter o Instant para o horário de Brasília
        ZonedDateTime brasiliaTime = instant.atZone(ZoneId.of("America/Sao_Paulo"));

        // Formatar para o padrão desejado
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        return brasiliaTime.format(formatter);
    }
}