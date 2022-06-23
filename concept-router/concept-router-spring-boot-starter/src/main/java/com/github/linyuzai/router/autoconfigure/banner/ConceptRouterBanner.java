package com.github.linyuzai.router.autoconfigure.banner;

import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

public class ConceptRouterBanner {

    @SneakyThrows
    public static void print() {
        ClassPathResource resource = new ClassPathResource("concept/router/banner.txt");
        try (InputStream is = resource.getInputStream()) {
            byte[] bytes = new byte[is.available()];
            int read = is.read(bytes);
            String banner = new String(bytes);
            System.out.println(build(banner, "Concept Router", "v0.5.0"));
        }
    }

    public static String build(String banner, String name, String version) {
        StringBuilder builder = new StringBuilder(banner);
        String tag = " :: " + name + " :: ";
        builder.append("\n").append(tag);
        int count = 70 - tag.length() - version.length();
        for (int i = 0; i < count; i++) {
            builder.append(" ");
        }
        return builder.append(version).toString();
    }
}
