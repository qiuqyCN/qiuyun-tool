/**
 * 用户状态枚举
 * 对应后端 UserStatus 枚举
 */
export enum UserStatus {
  DISABLED = 'DISABLED',  // 禁用
  ENABLED = 'ENABLED'     // 正常
}

/**
 * 评论类型枚举
 * 对应后端 ReviewType 枚举
 */
export enum ReviewType {
  REVIEW = 'REVIEW',  // 评论
  REPLY = 'REPLY'     // 回复
}

/**
 * 评论状态枚举
 * 对应后端 ReviewStatus 枚举
 */
export enum ReviewStatus {
  DISABLED = 0,  // 禁用
  ENABLED = 1    // 正常
}
