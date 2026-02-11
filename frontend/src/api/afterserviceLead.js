import request from './request'

export function getAfterserviceLeads(params = {}) {
  return request({
    url: '/api/afterservice-leads',
    method: 'get',
    params
  })
}

export function getAllAfterserviceLeads(afterServiceProjectId) {
  return request({
    url: '/api/afterservice-leads/all',
    method: 'get',
    params: { afterServiceProjectId }
  })
}

export function deleteAfterserviceLead(id) {
  return request({
    url: `/api/afterservice-leads/${id}`,
    method: 'delete'
  })
}

export function createAfterserviceLead(data) {
  return request({
    url: '/api/afterservice-leads',
    method: 'post',
    data
  })
}

export function updateAfterserviceLead(id, data) {
  return request({
    url: `/api/afterservice-leads/${id}`,
    method: 'put',
    data
  })
}
