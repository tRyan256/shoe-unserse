import levelData from 'province-city-china/dist/level.json';

export interface Region {
  code: string;
  name: string;
  province?: string;
  city?: string;
  area?: string;
  children?: Region[];
}

// 使用 province-city-china 包的完整省市区数据
export const regions: Region[] = levelData as Region[];

export function findProvinceByCode(code: string): Region | undefined {
  return regions.find((p) => p.code === code);
}

export function findCityByCode(provinceCode: string, cityCode: string): Region | undefined {
  const province = findProvinceByCode(provinceCode);
  return province?.children?.find((c) => c.code === cityCode);
}

export function findDistrictByCode(
  provinceCode: string,
  cityCode: string,
  districtCode: string
): Region | undefined {
  const city = findCityByCode(provinceCode, cityCode);
  return city?.children?.find((d) => d.code === districtCode);
}

export function getCitiesByProvince(provinceCode: string): Region[] {
  const province = findProvinceByCode(provinceCode);
  return province?.children || [];
}

export function getDistrictsByCity(provinceCode: string, cityCode: string): Region[] {
  const city = findCityByCode(provinceCode, cityCode);
  return city?.children || [];
}
