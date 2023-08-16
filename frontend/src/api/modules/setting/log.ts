import MSR from '@/api/http/index';
import { GetLogListUrl, GetLogOptionstUrl } from '@/api/requrls/setting/log';

import type { CommonList } from '@/models/common';
import type { LogOptions, LogItem } from '@/models/setting/log';

// 获取日志列表
export function getLogList(data: any) {
  return MSR.post<CommonList<LogItem>>({ url: GetLogListUrl, data });
}

// 获取日志操作范围选项
export function getLogOptions() {
  return MSR.get<LogOptions>({ url: GetLogOptionstUrl });
}
