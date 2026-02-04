import dayjs from 'dayjs'

/**
 * 格式化时间为标准日期时间格式
 * @param time 时间字符串
 * @param format 格式化模板，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns 格式化后的时间字符串
 */
export const formatDateTime = (time: string | undefined, format = 'YYYY-MM-DD HH:mm:ss'): string => {
  if (!time) return '-'
  return dayjs(time).format(format)
}

/**
 * 格式化时间为日期格式
 * @param time 时间字符串
 * @returns 格式化后的日期字符串
 */
export const formatDate = (time: string | undefined): string => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD')
}
