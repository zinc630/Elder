import { createRouter, createWebHistory } from "vue-router";
import Login from "../views/Login.vue";
import ElderHome from "../views/elder/ElderHome.vue";
import ChildHome from "../views/child/ChildHome.vue";
import AdminLayout from "../views/admin/AdminLayout.vue";
import AdminDashboard from "../views/admin/AdminDashboard.vue";
import AdminHealth from "../views/admin/AdminHealth.vue";
import AdminDispatch from "../views/admin/AdminDispatch.vue";
import AdminFamily from "../views/admin/AdminFamily.vue";
import AdminReports from "../views/admin/AdminReports.vue";
import ElderAlarmCenter from "../views/elder/ElderAlarmCenter.vue";
import ChildAlarmList from "../views/child/ChildAlarmList.vue";
import AdminPendingCalls from "../views/admin/AdminPendingCalls.vue";
import AdminPortalContent from "../views/admin/AdminPortalContent.vue";
import Register from "../views/Register.vue";
import AgencyHome from "../views/agency/AgencyHome.vue";
import SystemHome from "../views/SystemHome.vue";

const routes = [
  { path: "/", component: SystemHome },
  { path: "/login", component: Login },
  { path: "/elder/home", component: ElderHome },
  { path: "/elder/alarms", component: ElderAlarmCenter },
  { path: "/child/home", component: ChildHome },
  { path: "/child/alarms", component: ChildAlarmList },
  { path: "/admin/home", redirect: "/admin/dashboard" },
  {
    path: "/admin",
    component: AdminLayout,
    redirect: "/admin/dashboard",
    beforeEnter() {
      const role = localStorage.getItem("elder.role");
      if (role === "ADMIN") return true;
      return { path: "/login", query: { role: "ADMIN" } };
    },
    children: [
      { path: "dashboard", component: AdminDashboard },
      { path: "health", component: AdminHealth },
      { path: "dispatch", component: AdminDispatch },
      { path: "family", component: AdminFamily },
      { path: "reports", component: AdminReports },
      { path: "pending-calls", component: AdminPendingCalls },
      { path: "portal-content", component: AdminPortalContent }
    ]
  },
  { path: "/register", component: Register },
  {
    path: "/agency/home",
    component: AgencyHome,
    beforeEnter() {
      const role = localStorage.getItem("elder.role");
      if (role === "AGENCY" || role === "ADMIN") return true;
      return { path: "/login", query: { role: "AGENCY" } };
    }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;

