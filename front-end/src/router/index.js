import {createRouter, createWebHashHistory} from "vue-router";
import Home from "../views/Home.vue";
import HomeUser from "../views/user/HomeUser.vue";

const routes = [
    {
        path: '/',
        redirect: '/dashboard'
    }, {
        path: "/",
        name: "Home",
        component: Home,
        children: [
            {
                path: "/dashboard",
                name: "dashboard",
                meta: {
                    title: '系统首页',
                    roles:['admin']
                },
                component: () => import ( /* webpackChunkName: "dashboard" */ "../views/Dashboard.vue")
            }, {
                path: "/table",
                name: "basetable",
                meta: {
                    title: '查找所有用户',
                    roles:['admin']
                },
                component: () => import ( /* webpackChunkName: "table" */ "../views/BaseTable.vue")
            }, {
                path: "/charts",
                name: "basecharts",
                meta: {
                    title: '统计图表',
                    roles:['admin']
                },
                component: () => import ( /* webpackChunkName: "charts" */ "../views/BaseCharts.vue")
            }, {
                path: "/form",
                name: "baseform",
                meta: {
                    title: '图书管理',
                    roles:['admin']
                },
                component: () => import ( /* webpackChunkName: "form" */ "../views/BaseForm.vue")
            }, {
                path: "/tabs",
                name: "tabs",
                meta: {
                    title: '借阅信息管理',
                    roles:['admin']
                },
                component: () => import ( /* webpackChunkName: "tabs" */ "../views/Tabs.vue")
            }, {
                path: "/donate",
                name: "donate",
                meta: {
                    title: '鼓励作者',
                    roles:['admin']
                },
                component: () => import ( /* webpackChunkName: "donate" */ "../views/Donate.vue")
            }, {
                path: "/permission",
                name: "permission",
                meta: {
                    title: '权限管理',
                    permission: true,
                    roles:['admin']
                },
                component: () => import ( /* webpackChunkName: "permission" */ "../views/Permission.vue")
            }, {
                path: "/i18n",
                name: "i18n",
                meta: {
                    title: '国际化语言',
                    roles:['admin']
                },
                component: () => import ( /* webpackChunkName: "i18n" */ "../views/I18n.vue")
            }, {
                path: "/upload",
                name: "upload",
                meta: {
                    title: '图书类型管理',
                    roles:['admin']
                },
                component: () => import ( /* webpackChunkName: "upload" */ "../views/Upload.vue")
            }, {
                path: '/user',
                name: 'user',
                meta: {
                    title: '个人中心',
                    roles:['admin']
                },
                component: () => import (/* webpackChunkName: "user" */ '../views/User.vue')
            }, {
                path: '/editor',
                name: 'editor',
                meta: {
                    title: '富文本编辑器',
                    roles:['admin']
                },
                component: () => import (/* webpackChunkName: "editor" */ '../views/Editor.vue')
            },
            {
                path: '/examine',
                name: 'examine',
                meta: {
                    title: '审批管理',
                    roles:['admin']
                },
                component: () => import (/* webpackChunkName: "editor" */ '../views/examine.vue')
            }
        ]
    }, {
        path: "/login",
        name: "Login",
        meta: {
            title: '登录',
            roles:['admin','user']
        },
        component: () => import ( /* webpackChunkName: "login" */ "../views/Login.vue")
    },{
        path: '/403',
        name: '403',
        meta: {
            title: '没有权限',
            roles:['admin','user']
        },
        component: () => import (/* webpackChunkName: "403" */ '../views/403.vue')
    }, {
        path: '/404',
        name: '404',
        meta: {
            title: '找不到页面',
            roles:['admin','user']
        },
        component: () => import (/* webpackChunkName: "404" */ '../views/404.vue')
    }, {
        path: "/readerpage",
        name: "readerpage",
        component: HomeUser,
        redirect: '/bookslist',
        children: [
            {
                path: "/bookslist",
                name: "BooksList",
                meta: {
                    title: '读者首页',
                    roles:['admin','user']
                },
                component: () => import ( "../views/user/BooksList.vue")
            },
            {
                path: '/book/:isbn',
                name: 'BookInfo',
                meta: {
                    title: '书籍介绍',
                    roles:['admin','user']
                },
                component: () => import ("../views/user/BookInfo.vue")
            },
            {
                path: '/bookgrid',
                name: 'BookGrid',
                meta: {
                    title: '查阅书籍',
                    roles:['admin','user']
                },
                component: () => import ("../views/user/BookGrid.vue")
            }, {
                path: '/borrowbook',
                name: 'BorrowBook',
                meta: {
                    title: '书籍借阅',
                    roles:['user']
                },
                component: () => import ("../views/user/BorrowBook.vue")
            },{
                path: '/reader',
                name: 'reader',
                meta: {
                    title: '个人中心',
                    roles:['user']
                },
                component: () => import ("../views/user/Reader.vue")
            },]
    }
];

const router = createRouter({
    history: createWebHashHistory(),
    routes
});

router.beforeEach((to, from, next) => {
    document.title = `${to.meta.title} | 智慧图书馆`;
    const role = localStorage.getItem('role');
    let token = localStorage.getItem('token');
    if (token || to.path === '/login') {
        if(role==='admin'&&to.meta.roles.includes('admin')){
            next();
        }else if(role==='user'&&to.meta.roles.includes('user')){
            next();
        }else if(to.meta.roles.includes('user')){
            next();
        }else if(!to.matched.length){
            next('/404')
        }
        else {
            next('/403');
        }
    }else{
        next('/login');
    }
    // else if (!to.matched.length) {
    //     // 如果是管理员权限则可进入，这里只是简单的模拟管理员权限而已
    //     next('/404');
    // }
    // const role = localStorage.getItem('ms_username');
    // if (to.matched.length&&!role && to.path !== '/login') {
    //     //有值正常跳转
    //     next('login')
    // } else {
    //     //无值跳转404
    //     next({
    //         path: '/404',
    //     })
    // }

});

export default router;
