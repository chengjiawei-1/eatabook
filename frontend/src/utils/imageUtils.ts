// src/utils/imageUtils.ts

/**
 * 获取完整的图片URL
 * @param url 图片路径（相对或绝对）
 * @returns 完整的图片URL
 */
// 默认图片的base64编码SVG
const defaultImageSVG = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAwIiBoZWlnaHQ9IjMwMCIgdmlld0JveD0iMCAwIDQwMCAzMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CiAgPHJlY3Qgd2lkdGg9IjQwMCIgaGVpZ2h0PSIzMDAiIGZpbGw9IiNlOWU5ZTkiLz4KICA8dGV4dCB4PSIyMDAiIHk9IjE1MCIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjE4IiBmaWxsPSIjNjY2NjY2IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIj7pu5jorqTlm77niYc8L3RleHQ+Cjwvc3ZnPg==';

const defaultAvatarSVG = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgdmlld0JveD0iMCAwIDEwMCAxMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CiAgPGNpcmNsZSBjeD0iNTAiIGN5PSI0MCIgcj0iMjAiIGZpbGw9IiNlOWU5ZTkiLz4KICA8Y2lyY2xlIGN4PSI1MCIgY3k9Ijc1IiByPSIxNSIgZmlsbD0iI2U5ZTllOSIvPgo8L3N2Zz4=';

export const getImageUrl = (url: string): string => {
  if (!url || url === 'null' || url === 'undefined' || url.trim() === '') {
    return defaultImageSVG;
  }
  
  // 如果是完整URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('//')) {
    return url;
  }

  return url;
};

/**
 * 处理头像URL - 专门处理后端返回的相对路径
 * @param avatarUrl 头像URL
 * @returns 处理后的头像URL
 */
export const getAvatarUrl = (avatarUrl: string): string => {
  if (!avatarUrl || avatarUrl === 'null' || avatarUrl === 'undefined' || avatarUrl.trim() === '') {
    return defaultAvatarSVG;
  }
  
  // 开发环境：把旧服务器地址转换成当前前端代理可访问的相对路径
  if (avatarUrl.includes('118.178.122.36:8081')) {
    return avatarUrl.replace('http://118.178.122.36:8081', '');
  }
  
  // 处理历史数据中的绝对URL
  if (avatarUrl.includes('121.43.210.94')) {
    return avatarUrl.replace('http://121.43.210.94:8081', '');
  }
  
  // 处理其他开发环境的绝对URL
  if (avatarUrl.includes('172.24.64.1:8081')) {
    return avatarUrl.replace('http://172.24.64.1:8081', '');
  }
  
  if (avatarUrl.includes('192.168.254.27:8081')) {
    return avatarUrl.replace('http://192.168.254.27:8081', '');
  }
  
  return getImageUrl(avatarUrl);
};

/**
 * 处理论坛图片URL
 * @param imageUrl 论坛图片URL
 * @returns 处理后的图片URL
 */
export const getForumImageUrl = (imageUrl: string): string => {
  if (!imageUrl || imageUrl === 'null' || imageUrl === 'undefined' || imageUrl.trim() === '') {
    return '/images/default-image.png';
  }
  
  // 处理历史数据中的绝对URL
  if (imageUrl.includes('121.43.210.94')) {
    return imageUrl.replace('http://121.43.210.94:8081', '');
  }
  
  return getImageUrl(imageUrl);
};
