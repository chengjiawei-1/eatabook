import httpInstance from "@/utils/http"

//获取知识图谱
export function getGraph() {
    return httpInstance({
        url: '/api/graph',
        method: 'get',
        timeout: 30000000
    })
}
