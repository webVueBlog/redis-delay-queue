<template>
  <div class="home">
    <el-card class="welcome-card">
      <div class="welcome-content">
        <h1>欢迎使用Redis延迟队列管理系统</h1>
        <p>这是一个基于Redis的延迟队列系统，支持用户管理和权限控制</p>
      </div>
    </el-card>
    
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" @click="$router.push('/users')">
          <div class="stat-content">
            <el-icon class="stat-icon"><User /></el-icon>
            <div class="stat-info">
              <h3>用户管理</h3>
              <p>管理系统用户和权限</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card" @click="$router.push('/delay-queue')">
          <div class="stat-content">
            <el-icon class="stat-icon"><Clock /></el-icon>
            <div class="stat-info">
              <h3>延迟队列</h3>
              <p>Redis延迟队列服务</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card" @click="$router.push('/monitor')">
          <div class="stat-content">
            <el-icon class="stat-icon"><Monitor /></el-icon>
            <div class="stat-info">
              <h3>系统监控</h3>
              <p>实时监控系统状态</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
         <el-card class="stat-card" @click="$router.push('/menus')">
           <div class="stat-content">
             <el-icon class="stat-icon"><Menu /></el-icon>
             <div class="stat-info">
               <h3>菜单管理</h3>
               <p>管理系统菜单结构</p>
             </div>
           </div>
         </el-card>
       </el-col>
      </el-row>
      
      <el-row :gutter="20" class="stats-row">
       <el-col :span="6">
         <el-card class="stat-card" @click="$router.push('/organizations')">
           <div class="stat-content">
             <el-icon class="stat-icon"><OfficeBuilding /></el-icon>
             <div class="stat-info">
               <h3>组织管理</h3>
               <p>管理组织架构</p>
             </div>
           </div>
         </el-card>
       </el-col>
       <el-col :span="6">
         <el-card class="stat-card">
           <div class="stat-content">
             <el-icon class="stat-icon"><DataBoard /></el-icon>
             <div class="stat-info">
               <h3>数据统计</h3>
               <p>查看系统数据统计</p>
             </div>
           </div>
         </el-card>
       </el-col>
       <el-col :span="6">
         <el-card class="stat-card">
           <div class="stat-content">
             <el-icon class="stat-icon"><Setting /></el-icon>
             <div class="stat-info">
               <h3>系统设置</h3>
               <p>配置系统参数</p>
             </div>
           </div>
         </el-card>
       </el-col>
       <el-col :span="6">
         <el-card class="stat-card">
           <div class="stat-content">
             <el-icon class="stat-icon"><Document /></el-icon>
             <div class="stat-info">
               <h3>操作日志</h3>
               <p>查看系统操作记录</p>
             </div>
           </div>
         </el-card>
       </el-col>
    </el-row>
    
    <el-card class="feature-card">
      <h2>系统功能</h2>
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="feature-item">
            <h3>用户管理</h3>
            <ul>
              <li>用户注册和登录</li>
              <li>角色权限管理</li>
              <li>用户信息维护</li>
            </ul>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="feature-item">
            <h3>延迟队列</h3>
            <ul>
              <li>基于Redis实现</li>
              <li>支持延迟任务调度</li>
              <li>高性能消息处理</li>
            </ul>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { User, Clock, Monitor, Menu, OfficeBuilding, DataBoard, Setting, Document } from '@element-plus/icons-vue'
</script>

<style scoped>
.home {
  padding: 20px;
}

.welcome-card {
  margin-bottom: 20px;
  text-align: center;
}

.welcome-content h1 {
  color: #409eff;
  margin-bottom: 10px;
}

.welcome-content p {
  color: #666;
  font-size: 16px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  font-size: 40px;
  color: #409eff;
  margin-right: 15px;
}

.stat-info h3 {
  margin: 0 0 5px 0;
  color: #333;
}

.stat-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.feature-card h2 {
  color: #333;
  margin-bottom: 20px;
}

.feature-item h3 {
  color: #409eff;
  margin-bottom: 10px;
}

.feature-item ul {
  list-style-type: none;
  padding: 0;
}

.feature-item li {
  padding: 5px 0;
  color: #666;
}

.feature-item li:before {
  content: "✓";
  color: #67c23a;
  margin-right: 8px;
}
</style>