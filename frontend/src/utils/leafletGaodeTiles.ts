import L from "leaflet";

/**
 * 高德 Web 墨卡托路网瓦片（无需 JS API Key，样式接近高德/腾讯地图）。
 * 叠加点位需使用 GCJ-02（火星坐标），与 WGS84 需自行纠偏。
 */
export function createGaodeRoadTileLayer() {
  return L.tileLayer(
    "https://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}",
    {
      subdomains: ["1", "2", "3", "4"],
      maxZoom: 18,
      attribution: '© <a href="https://lbs.amap.com/">高德软件</a>'
    }
  );
}

export function createOsmTileLayer() {
  return L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
  });
}
