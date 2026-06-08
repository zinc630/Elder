import coordtransform from "coordtransform";

/** WGS84（GPS / 国际通用）→ GCJ-02（高德/腾讯底图） */
export function wgs84ToGcj02(lng: number, lat: number): [number, number] {
  const pair = coordtransform.wgs84togcj02(lng, lat) as number[];
  return [pair[0], pair[1]];
}
