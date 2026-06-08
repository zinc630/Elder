package com.example.elder.service.elder;

import com.example.elder.dto.NearbyDoctorDto;
import com.example.elder.dto.NearbyHospitalDto;
import com.example.elder.dto.NearbyMedicalResponseDto;
import com.example.elder.service.map.AmapMedicalClient;
import com.example.elder.service.map.AmapMedicalClient.GeoPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class NearbyMedicalResourceService {

    private static final Map<String, List<NearbyHospitalDto>> HOSPITALS = Map.of(
            "hangzhou", List.of(
                    new NearbyHospitalDto("hz_h1", "杭州市第一人民医院", "上城区浣纱路261号", "1.1km", "0571-87914796"),
                    new NearbyHospitalDto("hz_h2", "西湖区社区卫生服务中心", "西湖区文三路998号", "0.5km", "0571-87902222"),
                    new NearbyHospitalDto("hz_h3", "浙江省中医院", "拱墅区体育场路548号", "2.3km", "0571-86613666")
            ),
            "beijing", List.of(
                    new NearbyHospitalDto("bj_h1", "北京协和医院", "东城区帅府园1号", "1.4km", "010-69156114"),
                    new NearbyHospitalDto("bj_h2", "朝阳区社区卫生服务站", "朝阳区幸福小区3号楼", "0.4km", "010-65001234"),
                    new NearbyHospitalDto("bj_h3", "北京大学第一医院", "西城区西什库大街8号", "3.2km", "010-83572211")
            ),
            "shanghai", List.of(
                    new NearbyHospitalDto("sh_h1", "上海交通大学医学院附属瑞金医院", "黄浦区瑞金二路197号", "1.6km", "021-64370045"),
                    new NearbyHospitalDto("sh_h2", "浦东新区社区卫生服务中心", "浦东新区银宵路128号", "0.7km", "021-58881234"),
                    new NearbyHospitalDto("sh_h3", "复旦大学附属中山医院", "徐汇区枫林路180号", "2.8km", "021-64041990")
            ),
            "shenzhen", List.of(
                    new NearbyHospitalDto("sz_h1", "深圳市人民医院", "罗湖区东门北路1017号", "1.3km", "0755-25533018"),
                    new NearbyHospitalDto("sz_h2", "南山区社区卫生服务中心", "南山区科技园南路88号", "0.6km", "0755-26551234"),
                    new NearbyHospitalDto("sz_h3", "北京大学深圳医院", "福田区莲花路1120号", "2.5km", "0755-83923333")
            ),
            "guangzhou", List.of(
                    new NearbyHospitalDto("gz_h1", "中山大学附属第一医院", "越秀区中山二路58号", "1.5km", "020-87755766"),
                    new NearbyHospitalDto("gz_h2", "天河区社区卫生服务中心", "天河区体育西路120号", "0.8km", "020-38881234"),
                    new NearbyHospitalDto("gz_h3", "广东省人民医院", "越秀区中山二路106号", "2.1km", "020-83827812")
            )
    );

    private static final Map<String, List<NearbyDoctorDto>> DOCTORS = Map.of(
            "hangzhou", List.of(
                    new NearbyDoctorDto("hz_d1", "王建国", "主任医师", "杭州市第一人民医院", "老年心血管", "0571-88001001"),
                    new NearbyDoctorDto("hz_d2", "李芳", "副主任医师", "西湖区社区卫生服务中心", "全科 / 慢病管理", "0571-88001002"),
                    new NearbyDoctorDto("hz_d3", "陈明", "主治医师", "浙江省中医院", "中医康复", "0571-88001003")
            ),
            "beijing", List.of(
                    new NearbyDoctorDto("bj_d1", "张建华", "主任医师", "北京协和医院", "老年内科", "010-88001001"),
                    new NearbyDoctorDto("bj_d2", "刘敏", "副主任医师", "朝阳区社区卫生服务站", "家庭医生", "010-88001002"),
                    new NearbyDoctorDto("bj_d3", "赵强", "主治医师", "北京大学第一医院", "康复医学", "010-88001003")
            ),
            "shanghai", List.of(
                    new NearbyDoctorDto("sh_d1", "周伟", "主任医师", "瑞金医院", "心脑血管", "021-88001001"),
                    new NearbyDoctorDto("sh_d2", "吴婷", "副主任医师", "浦东新区社区卫生服务中心", "全科诊疗", "021-88001002"),
                    new NearbyDoctorDto("sh_d3", "孙浩", "主治医师", "复旦大学附属中山医院", "老年医学", "021-88001003")
            ),
            "shenzhen", List.of(
                    new NearbyDoctorDto("sz_d1", "黄志明", "主任医师", "深圳市人民医院", "老年病科", "0755-88001001"),
                    new NearbyDoctorDto("sz_d2", "林雪", "副主任医师", "南山区社区卫生服务中心", "慢病随访", "0755-88001002"),
                    new NearbyDoctorDto("sz_d3", "郑磊", "主治医师", "北京大学深圳医院", "中医调理", "0755-88001003")
            ),
            "guangzhou", List.of(
                    new NearbyDoctorDto("gz_d1", "梁国栋", "主任医师", "中山大学附属第一医院", "老年心血管", "020-88001001"),
                    new NearbyDoctorDto("gz_d2", "何丽", "副主任医师", "天河区社区卫生服务中心", "全科 / 护理", "020-88001002"),
                    new NearbyDoctorDto("gz_d3", "冯杰", "主治医师", "广东省人民医院", "康复理疗", "020-88001003")
            )
    );

    private static final Map<String, String> REGION_LABELS = Map.of(
            "hangzhou", "杭州",
            "beijing", "北京",
            "shanghai", "上海",
            "shenzhen", "深圳",
            "guangzhou", "广州"
    );

    private final ElderDbService elderDbService;
    private final AmapMedicalClient amapMedicalClient;

    public NearbyMedicalResourceService(ElderDbService elderDbService, AmapMedicalClient amapMedicalClient) {
        this.elderDbService = elderDbService;
        this.amapMedicalClient = amapMedicalClient;
    }

    public NearbyMedicalResponseDto<NearbyHospitalDto> hospitalsForElder(String elderId) {
        String address = resolveUserAddress(elderId);
        String regionKey = resolveRegionKeyFromAddress(address);
        String regionLabel = resolveRegionLabel(address, regionKey);

        if (amapMedicalClient.isAvailable() && address != null && !address.isBlank()) {
            Optional<GeoPoint> point = amapMedicalClient.geocode(address);
            if (point.isPresent()) {
                List<NearbyHospitalDto> items = amapMedicalClient.searchHospitals(point.get());
                if (!items.isEmpty()) {
                    GeoPoint p = point.get();
                    return new NearbyMedicalResponseDto<>(
                            "amap",
                            address,
                            regionLabel,
                            p.lng() + "," + p.lat(),
                            items
                    );
                }
            }
        }

        return demoHospitals(address, regionKey, regionLabel);
    }

    public NearbyMedicalResponseDto<NearbyDoctorDto> doctorsForElder(String elderId) {
        String address = resolveUserAddress(elderId);
        String regionKey = resolveRegionKeyFromAddress(address);
        String regionLabel = resolveRegionLabel(address, regionKey);

        if (amapMedicalClient.isAvailable() && address != null && !address.isBlank()) {
            Optional<GeoPoint> point = amapMedicalClient.geocode(address);
            if (point.isPresent()) {
                List<NearbyDoctorDto> items = amapMedicalClient.searchClinics(point.get());
                if (!items.isEmpty()) {
                    GeoPoint p = point.get();
                    return new NearbyMedicalResponseDto<>(
                            "amap",
                            address,
                            regionLabel,
                            p.lng() + "," + p.lat(),
                            items
                    );
                }
            }
        }

        return demoDoctors(address, regionKey, regionLabel);
    }

    private NearbyMedicalResponseDto<NearbyHospitalDto> demoHospitals(
            String address, String regionKey, String regionLabel
    ) {
        return new NearbyMedicalResponseDto<>(
                "demo",
                blank(address),
                regionLabel,
                "",
                new ArrayList<>(HOSPITALS.getOrDefault(regionKey, HOSPITALS.get("hangzhou")))
        );
    }

    private NearbyMedicalResponseDto<NearbyDoctorDto> demoDoctors(
            String address, String regionKey, String regionLabel
    ) {
        return new NearbyMedicalResponseDto<>(
                "demo",
                blank(address),
                regionLabel,
                "",
                new ArrayList<>(DOCTORS.getOrDefault(regionKey, DOCTORS.get("hangzhou")))
        );
    }

    private String resolveUserAddress(String elderId) {
        return elderDbService.getElderProfile(elderId)
                .map(p -> p.getAddress())
                .orElse("");
    }

    static String resolveRegionKeyFromAddress(String address) {
        if (address == null || address.isBlank()) {
            return "hangzhou";
        }
        String a = address.toLowerCase(Locale.ROOT);
        if (a.contains("北京")) return "beijing";
        if (a.contains("上海")) return "shanghai";
        if (a.contains("深圳")) return "shenzhen";
        if (a.contains("广州")) return "guangzhou";
        if (a.contains("杭州") || a.contains("西湖") || a.contains("拱墅") || a.contains("上城")) return "hangzhou";
        return "hangzhou";
    }

    /** 展示用城市名：优先从住址解析，避免非杭州地址仍显示「杭州」 */
    static String resolveRegionLabel(String address, String regionKey) {
        if (address != null && !address.isBlank()) {
            int cityIdx = address.indexOf('市');
            if (cityIdx > 0 && cityIdx <= 8) {
                return address.substring(0, cityIdx + 1);
            }
            if (address.contains("石家庄") || address.contains("河北")) {
                return "石家庄";
            }
        }
        return REGION_LABELS.getOrDefault(regionKey, "本地");
    }

    private static String blank(String s) {
        return s == null ? "" : s.trim();
    }
}
