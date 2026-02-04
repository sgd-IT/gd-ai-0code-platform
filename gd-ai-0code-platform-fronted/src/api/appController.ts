// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /app/add */
export async function addApp(body: API.AppAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong>('/app/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 DELETE /app/admin/delete */
export async function adminDeleteApp(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/app/admin/delete', {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /app/admin/get/${param0} */
export async function adminGetAppById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.adminGetAppByIdParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.BaseResponseApp>(`/app/admin/get/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /app/admin/list/page/vo */
export async function adminListAppByPage(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.adminListAppByPageParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAppVO>('/app/admin/list/page/vo', {
    method: 'GET',
    params: {
      ...params,
      appAdminQueryRequest: undefined,
      ...params['appAdminQueryRequest'],
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 PUT /app/admin/update */
export async function adminUpdateApp(
  body: API.AppAdminUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/app/admin/update', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /app/chat/gen/code */
export async function chatToGenCode(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.chatToGenCodeParams,
  options?: { [key: string]: any }
) {
  return request<API.ServerSentEventString[]>('/app/chat/gen/code', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 DELETE /app/delete */
export async function deleteApp(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/app/delete', {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /app/deploy */
export async function deployApp(body: API.AppDeployRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/app/deploy', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /app/get/vo */
export async function getAppVoById(options?: { [key: string]: any }) {
  return request<API.BaseResponseAppVO>('/app/get/vo', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /app/list/page/vo */
export async function listMyAppVoByPage(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listMyAppVOByPageParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAppVO>('/app/list/page/vo', {
    method: 'GET',
    params: {
      ...params,
      appQueryRequest: undefined,
      ...params['appQueryRequest'],
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /app/list/page/vo/featured */
export async function listFeaturedAppVoByPage(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listFeaturedAppVOByPageParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAppVO>('/app/list/page/vo/featured', {
    method: 'GET',
    params: {
      ...params,
      appQueryRequest: undefined,
      ...params['appQueryRequest'],
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 PUT /app/update */
export async function updateApp(body: API.AppUpdateRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/app/update', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
