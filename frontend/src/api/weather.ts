import axios from 'axios';

const KEY = import.meta.env.VITE_QWEATHER_API_KEY || '';
const GEO_BASE = '/weather-api/geo/v2';
const WEATHER_BASE = '/weather-api/v7';

// 定义城市搜索结果的接口
export interface CityInfo {
  name: string;
  id: string;
  adm1: string; // 省份
  adm2: string; // 城市
}

/**
 * 城市搜索 - 获取 Location ID
 * @param location 城市名称，如 "北京" 或 "chaoyang"
 */
export const searchCity = async (location: string): Promise<CityInfo | null> => {
  try {
    if (!KEY) {
      console.error("VITE_QWEATHER_API_KEY is not configured");
      return null;
    }

    const res = await axios.get(`${GEO_BASE}/city/lookup`, {
      params: {
        location: location,
        key: KEY,
        range: 'cn', // 仅限中国城市搜索
        number: 1    // 我们通常只需要匹配度最高的那一个
      }
    });

    if (res.data.code === '200' && res.data.location?.length > 0) {
      return res.data.location[0] as CityInfo;
    }
    return null;
  } catch (error) {
    console.error("城市搜索请求失败:", error);
    return null;
  }
};

/**
 * 获取 7 天天气预报
 * @param locationId 城市搜索拿到的 id
 */
export const get7DayWeather = async (locationId: string) => {
  if (!KEY) {
    throw new Error("VITE_QWEATHER_API_KEY is not configured");
  }

  const res = await axios.get(`${WEATHER_BASE}/weather/7d`, {
    params: {
      location: locationId,
      key: KEY
    }
  });
  return res.data.daily;
};
