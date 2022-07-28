<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item>
                    <i class="el-icon-pie-chart"></i> 统计图表
                </el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="container">
          <el-row :gutter="20">
            <!--        <el-col :xs="24" :sm="24" :lg="8">-->
            <!--          <div class="chart-wrapper">-->
            <!--            <raddar-chart />-->
            <!--          </div>-->
            <!--        </el-col>-->
            <el-col :xs="24" :sm="24" :lg="12">
              <div class="chart-wrapper">
                <raddar-chart/>
              </div>
            </el-col>
            <el-col :xs="24" :sm="24" :lg="12">
              <div class="chart-wrapper">
                <line-chart/>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="24" :lg="12">
              <div class="chart-wrapper">
                <pie-chart/>
              </div>
            </el-col>
            <el-col :xs="24" :sm="24" :lg="12">
              <div class="chart-wrapper">
                <bar-chart/>
              </div>
            </el-col>
          </el-row>
<!--            <div class="schart-box">-->
<!--                <div class="content-title">柱状图</div>-->
<!--                <schart class="schart" canvasId="bar" :options="options1"></schart>-->
<!--            </div>-->
<!--            <div class="schart-box">-->
<!--                <div class="content-title">折线图</div>-->
<!--                <schart class="schart" canvasId="line" :options="options2"></schart>-->
<!--            </div>-->
<!--            <div class="schart-box">-->
<!--                <div class="content-title">饼状图</div>-->
<!--                <schart class="schart" canvasId="pie" :options="options3"></schart>-->
<!--            </div>-->
<!--            <div class="schart-box">-->
<!--                <div class="content-title">环形图</div>-->
<!--                <schart class="schart" canvasId="ring" :options="options4"></schart>-->
<!--            </div>-->
        </div>
    </div>
</template>

<script>
import Schart from "vue-schart";
import PieChart from "./chart/PieChart.vue";
import BarChart from "./chart/BarChart.vue";
import RaddarChart from "./chart/RaddarChart.vue";
import LineChart from "./chart/LineChart.vue";
export default {
    name: "basecharts",
    components: {
        Schart,
        PieChart,
        BarChart,
        RaddarChart,
        LineChart
    },
    setup() {
        const options1 = {
            type: "bar",
            title: {
                text: "最近一周图书借阅情况",
            },
            bgColor: "#fbfbfb",
            labels: ["周一", "周二", "周三", "周四", "周五"],
            datasets: [
                {
                    label: "家电",
                    fillColor: "rgba(241, 49, 74, 0.5)",
                    data: [234, 278, 270, 190, 230],
                },
                {
                    label: "百货",
                    data: [164, 178, 190, 135, 160],
                },
                {
                    label: "食品",
                    data: [144, 198, 150, 235, 120],
                },
            ],
        };
        const options2 = {
            type: "line",
            title: {
                text: "最近几个月各类书籍借阅趋势图",
            },
            bgColor: "#fbfbfb",
            labels: ["6月", "7月", "8月", "9月", "10月"],
            datasets: [
                {
                    label: "家电",
                    data: [234, 278, 270, 190, 230],
                },
                {
                    label: "百货",
                    data: [164, 178, 150, 135, 160],
                },
                {
                    label: "食品",
                    data: [114, 138, 200, 235, 190],
                },
            ],
        };
        const options3 = {
            type: "pie",
            title: {
                text: "所藏各类书籍饼状图",
            },
            legend: {
                position: "left",
            },
            bgColor: "#fbfbfb",
            labels: [
                "T恤",
                "牛仔裤",
                "连衣裙",
                "毛衣",
                "七分裤",
                "短裙",
                "羽绒服",
            ],
            datasets: [
                {
                    data: [334, 278, 190, 235, 260, 200, 141],
                },
            ],
        };
        const options4 = {
            type: "ring",
            title: {
                text: "环形三等分",
            },
            showValue: false,
            legend: {
                position: "bottom",
                bottom: 40,
            },
            bgColor: "#fbfbfb",
            labels: ["vue", "react", "angular"],
            datasets: [
                {
                    data: [500, 500, 500],
                },
            ],
        };
        return {
            options1,
            options2,
            options3,
            options4,
        };
    },
};
</script>

<style scoped>
.schart-box {
    display: inline-block;
    margin: 20px;
}
.schart {
    width: 600px;
    height: 400px;
}
.content-title {
    clear: both;
    font-weight: 400;
    line-height: 50px;
    margin: 10px 0;
    font-size: 22px;
    color: #1f2f3d;
}
.chart-wrapper {
  background: #fff;
  padding: 16px 16px 0;
  margin-bottom: 32px;
}
</style>
