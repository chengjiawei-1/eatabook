<template>
  <div ref="mapChart" style="width: 100%; height: 600px;"></div>
</template>

<script>
import * as echarts from 'echarts';
import 'echarts/map/js/china.js'; // 引入中国地图数据

export default {
  mounted() {
    this.initChart();
  },
  methods: {
    async initChart() {
      const chart = echarts.init(this.$refs.mapChart);
      
      // 或者动态加载地图数据
      // const chinaJson = await import('echarts/map/json/china.json');
      // echarts.registerMap('china', chinaJson);
      
      // 模拟数据
      const data = [
  // 4个直辖市
  { name: '北京市', value: 1000 },
  { name: '天津市', value: 800 },
  { name: '上海市', value: 950 },
  { name: '重庆市', value: 750 },
  
  // 23个省份
  { name: '河北省', value: 600 },
  { name: '山西省', value: 590 },
  { name: '辽宁省', value: 650 },
  { name: '吉林省', value: 570 },
  { name: '黑龙江省', value: 550 },
  { name: '江苏省', value: 880 },
  { name: '浙江省', value: 920 },
  { name: '安徽省', value: 680 },
  { name: '福建省', value: 730 },
  { name: '江西省', value: 620 },
  { name: '山东省', value: 850 },
  { name: '河南省', value: 700 },
  { name: '湖北省', value: 710 },
  { name: '湖南省', value: 720 },
  { name: '广东省', value: 980 },
  { name: '海南省', value: 460 },
  { name: '四川省', value: 820 },
  { name: '贵州省', value: 520 },
  { name: '云南省', value: 500 },
  { name: '陕西省', value: 610 },
  { name: '甘肃省', value: 530 },
  { name: '青海省', value: 420 },
  { name: '台湾省', value: 490 },
  // 5个自治区
  { name: '内蒙古自治区', value: 480 },
  { name: '广西壮族自治区', value: 580 },
  { name: '西藏自治区', value: 350 },
  { name: '宁夏回族自治区', value: 440 },
  { name: '新疆维吾尔自治区', value: 450 },
  
  // 2个特别行政区
  { name: '香港特别行政区', value: 670 },
  { name: '澳门特别行政区', value: 380 }
  
];
      
      const option = {
        backgroundColor: '#1547a1',
        title: {
          text: '中国地图数据展示',
          subtext: '模拟数据',
          left: 'center',
          textStyle: {
            color: '#fff'
          }
        },
        tooltip: {
          trigger: 'item'
        },
        visualMap: {
          min: 0,
          max: 200,
          left: 'left',
          top: 'bottom',
          text: ['高','低'],
          calculable: true,
          inRange: {
            color: ['lightskyblue', 'yellow', 'orangered']
          },
          textStyle: {
            color: '#fff'
          }
        },
        series: [{
          name: '数据',
          type: 'map',
          map: 'china',
          roam: true,
          label: {
            show: true,
            color: '#fff'
          },
          data: data,
          emphasis: {
            itemStyle: {
              areaColor: '#ffcc00'
            }
          }
        }]
      };
      
      chart.setOption(option);
    }
  }
};
</script>