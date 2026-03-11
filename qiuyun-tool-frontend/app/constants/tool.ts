/**
 * 工具页面通用常量
 */

import type { CharsetOption, BaseOption } from '~/types/tool-common'

/**
 * 字符集选项
 * 用于 URL 编解码、MD5、Base64 等工具
 */
export const CHARSET_OPTIONS: CharsetOption[] = [
  { value: 'UTF-8', label: 'UTF-8 (推荐)' },
  { value: 'GBK', label: 'GBK (中文)' },
  { value: 'ISO-8859-1', label: 'ISO-8859-1 (西欧)' },
  { value: 'ASCII', label: 'ASCII' }
]

/**
 * 进制选项
 * 用于进制转换工具
 */
export const BASE_OPTIONS: BaseOption[] = [
  { value: 2, label: '二进制 (Binary)', prefix: '0b', example: '1010' },
  { value: 8, label: '八进制 (Octal)', prefix: '0o', example: '12' },
  { value: 10, label: '十进制 (Decimal)', prefix: '', example: '10' },
  { value: 16, label: '十六进制 (Hex)', prefix: '0x', example: 'A' }
]

/**
 * 代码美化类型选项
 */
export const CODE_BEAUTIFY_OPTIONS = [
  { value: 'json', label: 'JSON', mode: 'json' },
  { value: 'html', label: 'HTML', mode: 'htmlmixed' },
  { value: 'css', label: 'CSS', mode: 'css' },
  { value: 'javascript', label: 'JavaScript', mode: 'javascript' },
  { value: 'sql', label: 'SQL', mode: 'sql' },
  { value: 'xml', label: 'XML', mode: 'xml' }
]

/**
 * 时区选项
 * 用于时间戳转换工具
 */
export const TIMEZONE_OPTIONS = [
  { value: 'UTC', label: 'UTC 标准时间' },
  { value: 'Local', label: '本地时间' },
  { value: 'Asia/Shanghai', label: '北京时间' },
  { value: 'Asia/Tokyo', label: '东京时间' },
  { value: 'America/New_York', label: '纽约时间' },
  { value: 'Europe/London', label: '伦敦时间' }
]

/**
 * 日期格式选项
 * 用于时间戳转换工具
 */
export const DATE_FORMAT_OPTIONS = [
  { value: 'yyyy-MM-dd HH:mm:ss', label: '标准格式 (2024-01-15 10:30:00)' },
  { value: 'yyyy-MM-dd', label: '日期格式 (2024-01-15)' },
  { value: 'yyyy/MM/dd HH:mm:ss', label: '斜杠格式 (2024/01/15 10:30:00)' },
  { value: 'dd/MM/yyyy HH:mm:ss', label: '欧洲格式 (15/01/2024 10:30:00)' },
  { value: 'MM-dd-yyyy HH:mm:ss', label: '美国格式 (01-15-2024 10:30:00)' },
  { value: 'yyyy年MM月dd日 HH:mm:ss', label: '中文格式 (2024年01月15日 10:30:00)' }
]
