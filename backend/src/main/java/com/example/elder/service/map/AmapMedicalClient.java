package com.example.elder.service.map;

import com.example.elder.config.AmapProperties;
import com.example.elder.dto.NearbyDoctorDto;
import com.example.elder.dto.NearbyHospitalDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AmapMedicalClient {

    private static final Logger log = LoggerFactory.getLogger(AmapMedicalClient.class);

    private final AmapProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public AmapMedicalClient(AmapProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
    }

    public boolean isAvailable() {
        return properties.isConfigured();
    }

    public Optional<GeoPoint> geocode(String address) {
        if (!isAvailable() || address == null || address.isBlank()) {
            return Optional.empty();
        }
        try {
            String body = restClient.get()
                    .uri(uri -> uri.scheme("https")
                            .host("restapi.amap.com")
                            .path("/v3/geocode/geo")
                            .queryParam("key", properties.getWebKey())
                            .queryParam("address", address.trim())
                            .build())
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(body);
            if (!"1".equals(text(root, "status"))) {
                log.warn("Amap geocode failed: {}", text(root, "info"));
                return Optional.empty();
            }
            JsonNode geocodes = root.path("geocodes");
            if (!geocodes.isArray() || geocodes.isEmpty()) {
                return Optional.empty();
            }
            String location = text(geocodes.get(0), "location");
            if (location.isBlank() || !location.contains(",")) {
                return Optional.empty();
            }
            String[] parts = location.split(",");
            double lng = Double.parseDouble(parts[0].trim());
            double lat = Double.parseDouble(parts[1].trim());
            return Optional.of(new GeoPoint(lng, lat));
        } catch (Exception e) {
            log.warn("Amap geocode error: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public List<NearbyHospitalDto> searchHospitals(GeoPoint center) {
        return searchPois(center, "医院", "090100").stream()
                .map(p -> new NearbyHospitalDto(p.id(), p.name(), p.fullAddress(), p.distanceLabel(), p.phone()))
                .toList();
    }

    /** 诊所 / 社区卫生机构，作为「找医生」可预约门诊点 */
    public List<NearbyDoctorDto> searchClinics(GeoPoint center) {
        List<PoiRow> pois = new ArrayList<>();
        pois.addAll(searchPois(center, "社区卫生服务中心", null));
        pois.addAll(searchPois(center, "诊所", null));
        return pois.stream()
                .limit(properties.getMaxResults())
                .map(p -> new NearbyDoctorDto(
                        p.id(),
                        p.name(),
                        "门诊",
                        p.regionLabel(),
                        p.typeName().isBlank() ? "全科 / 基层医疗" : p.typeName(),
                        p.phone()
                ))
                .toList();
    }

    private List<PoiRow> searchPois(GeoPoint center, String keywords, String types) {
        if (!isAvailable()) {
            return List.of();
        }
        try {
            String body = restClient.get()
                    .uri(uri -> {
                        var b = uri.scheme("https")
                                .host("restapi.amap.com")
                                .path("/v3/place/around")
                                .queryParam("key", properties.getWebKey())
                                .queryParam("location", center.lng() + "," + center.lat())
                                .queryParam("keywords", keywords)
                                .queryParam("radius", properties.getSearchRadiusMeters())
                                .queryParam("offset", properties.getMaxResults())
                                .queryParam("extensions", "all");
                        if (types != null && !types.isBlank()) {
                            b.queryParam("types", types);
                        }
                        return b.build();
                    })
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(body);
            if (!"1".equals(text(root, "status"))) {
                log.warn("Amap place around failed: {}", text(root, "info"));
                return List.of();
            }
            JsonNode pois = root.path("pois");
            if (!pois.isArray()) {
                return List.of();
            }
            List<PoiRow> rows = new ArrayList<>();
            for (JsonNode poi : pois) {
                String id = text(poi, "id");
                if (id.isBlank()) {
                    id = "poi_" + rows.size();
                }
                String name = text(poi, "name");
                if (name.isBlank()) {
                    continue;
                }
                String address = text(poi, "address");
                String pname = text(poi, "pname");
                String cityname = text(poi, "cityname");
                String adname = text(poi, "adname");
                String fullAddress = joinAddress(pname, cityname, adname, address);
                String distance = formatDistance(text(poi, "distance"));
                String tel = firstPhone(text(poi, "tel"));
                String type = text(poi, "type");
                rows.add(new PoiRow(id, name, fullAddress, distance, tel, cityname, adname, type));
            }
            return rows;
        } catch (Exception e) {
            log.warn("Amap place around error: {}", e.getMessage());
            return List.of();
        }
    }

    private static String formatDistance(String metersRaw) {
        if (metersRaw == null || metersRaw.isBlank()) {
            return "";
        }
        try {
            int m = (int) Math.round(Double.parseDouble(metersRaw));
            if (m < 1000) {
                return m + "m";
            }
            return String.format(Locale.ROOT, "%.1fkm", m / 1000.0);
        } catch (NumberFormatException e) {
            return metersRaw;
        }
    }

    private static String firstPhone(String tel) {
        if (tel == null || tel.isBlank()) {
            return "暂无电话";
        }
        String[] parts = tel.split(";");
        return parts[0].trim();
    }

    private static String joinAddress(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p != null && !p.isBlank() && !sb.toString().contains(p)) {
                sb.append(p);
            }
        }
        return sb.isEmpty() ? "详见地图" : sb.toString();
    }

    private static String text(JsonNode node, String field) {
        if (node == null || node.isMissingNode()) {
            return "";
        }
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? "" : v.asText("").trim();
    }

    public record GeoPoint(double lng, double lat) {}

    private record PoiRow(
            String id,
            String name,
            String fullAddress,
            String distanceLabel,
            String phone,
            String cityname,
            String adname,
            String typeName
    ) {
        String regionLabel() {
            if (adname != null && !adname.isBlank()) {
                return adname;
            }
            return cityname != null ? cityname : "";
        }
    }
}
