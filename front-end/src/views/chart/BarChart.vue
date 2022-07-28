<template>
  <div :class="className" :style="{height:height,width:width}" ref="main"/>
</template>

<script>
import * as echarts from 'echarts';
// import 'echarts/theme/macarons.js'
// require('echarts/theme/macarons') // echarts theme
import resize from '../mixins/resize'
import request from "../../utils/request";
import {reactive, ref, toRaw} from "vue";
import {useStore} from "vuex";
export default {
  name:"PieChart",
  mixins: [resize],
  props: {
    className: {
      type: String,
      default: 'chart'
    },
    width: {
      type: String,
      default: '100%'
    },
    height: {
      type: String,
      default: '300px'
    }
  },
  setup() {
    const store = useStore();
    return{
      store
    }
  },
  computed: {
  },
  created() {
    this.getdata()
  },
  data() {
    return {
      analyzeNewReader:[],
      category:[],
      bookdata:[],
      chart: null,
      option:null,
    }
  },
  mounted() {

  },
  beforeDestroy() {
    if (!this.chart) {
      return
    }
    this.chart.dispose()
    this.chart = null
  },
  methods: {
    getdata(){
      request.get("/admin/statistics/Count").then(res => {
        if (res.code === 455) {
          this.analyzeNewReader=res.data.analyzeNewReader
          for(let i of this.analyzeNewReader) {
            this.category.push(i.name)
            this.bookdata.push(i.cnt)
          }
          this.$nextTick(() => {
            this.initChart()
          })
        }
      })
    },
    initChart() {
      this.chart = echarts.init(this.$refs.main)
      this.option = {
        title: {
          text: '用户一周新增数量',
          textStyle:{
            color:'#666666',
            textVerticalAlign: "auto"
          }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            crossStyle: {
              color: '#999'
            }
          }
        },
        toolbox: {
          feature: {
            // dataView: { show: true, readOnly: false },
            // magicType: { show: true, type: ['line', 'bar'] },
            // restore: { show: true },
            // saveAsImage: { show: true }
          }
        },
        legend: {
          show:false,
          data: ['Evaporation', 'Precipitation', 'Temperature']
        },
        xAxis: [
          {
            type: 'category',
            data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
            axisPointer: {
              type: 'shadow'
            }
          }
        ],
        yAxis: [
          {
            type: 'value',
            // name: '新增数',
            min: 0,
            max: 250,
            interval: 50,
            // axisLabel: {
            //   formatter: '{value} ml'
            // }
          },
          {
            type: 'value',
            // name: '新增数',
            min: 0,
            max: 250,
            interval: 50,
            // axisLabel: {
            //   formatter: '{value} ml'
            // }
          },
        ],
        series: [
          {
            name: '新增量',
            color:'#4a90e2',
            type: 'bar',
            data: [
              2.0, 4.9, 7.0, 23.2, 25.6, 76.7, 135.6, 162.2, 32.6, 20.0, 6.4, 3.3
            ]
          },
          {
            name: '新增量',
            color:'#ee6666',
            type: 'line',
            yAxisIndex: 1,
            data: [
              2.0, 4.9, 7.0, 23.2, 25.6, 76.7, 135.6, 162.2, 32.6, 20.0, 6.4, 3.3
            ]
          }
        ]
      };
      this.chart.setOption(this.option);
    }
  }
}
</script>
