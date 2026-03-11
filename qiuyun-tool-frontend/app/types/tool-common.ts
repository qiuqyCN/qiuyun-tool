/**
 * 工具通用类型定义
 * 所有工具页面共享的基础类型
 */

import type { ToolType } from './tool'

/**
 * 基础工具响应结果接口
 * 所有工具响应都应继承此接口
 */
export interface BaseToolResult {
  /** 是否成功 */
  success: boolean
  /** 错误信息 */
  errorMessage?: string
}

/**
 * 编码/解码操作类型
 */
export enum CodecOperation {
  ENCODE = 'encode',
  DECODE = 'decode'
}

/**
 * 字符集选项
 */
export interface CharsetOption {
  value: string
  label: string
}

/**
 * 进制选项
 */
export interface BaseOption {
  value: number
  label: string
  prefix: string
  example: string
}

/**
 * 工具执行配置
 */
export interface ToolExecuteConfig<TParams, TResult extends BaseToolResult> {
  toolCode: string
  toolType: ToolType
  onSuccess?: (result: TResult) => void
  onError?: (error: string) => void
}

/**
 * 文本对比差异类型
 */
export enum DiffType {
  ADDED = 'added',
  REMOVED = 'removed',
  MODIFIED = 'modified',
  UNCHANGED = 'unchanged'
}

/**
 * 随机数生成类型
 */
export enum RandomGeneratorType {
  INTEGER = 'integer',
  FLOAT = 'float',
  PASSWORD = 'password',
  STRING = 'string',
  UUID = 'uuid',
  BOOLEAN = 'boolean',
  CHOICE = 'choice'
}

/**
 * 代码美化类型
 */
export enum CodeBeautifyType {
  JSON = 'json',
  HTML = 'html',
  CSS = 'css',
  JAVASCRIPT = 'javascript',
  SQL = 'sql',
  XML = 'xml'
}
