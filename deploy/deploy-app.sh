#!/bin/bash
# 应用部署脚本

set -e

APP_DIR="/home/ubuntu/c-project"
BACKEND_PORT=8080
FRONTEND_PORT=5173

echo "=========================================="
echo "  部署 C语言数据文件智能处理平台"
echo "=========================================="

# 创建应用目录
echo "[1/5] 创建应用目录..."
mkdir -p $APP_DIR
cd $APP_DIR

# 如果代码已存在则更新，否则提示上传
if [ -d "backend" ] && [ -d "frontend" ]; then
    echo "代码目录已存在"
else
    echo "请先上传代码到 $APP_DIR 目录"
    echo "可以使用 scp 或 git clone"
    exit 1
fi

# 构建后端
echo "[2/5] 构建后端..."
cd $APP_DIR/backend
mvn clean package -DskipTests
echo "后端构建完成"

# 构建前端
echo "[3/5] 构建前端..."
cd $APP_DIR/frontend
npm install
npm run build
echo "前端构建完成"

# 启动后端服务
echo "[4/5] 启动后端服务..."
cd $APP_DIR/backend
nohup java -jar target/*.jar --server.port=$BACKEND_PORT > backend.log 2>&1 &
echo "后端服务已启动，端口: $BACKEND_PORT"

# 配置 Nginx (生产环境)
echo "[5/5] 配置 Nginx..."
sudo tee /etc/nginx/sites-available/c-project > /dev/null <<EOF
server {
    listen 80;
    server_name _;

    # 前端静态文件
    location / {
        root $APP_DIR/frontend/dist;
        index index.html;
        try_files \$uri \$uri/ /index.html;
    }

    # API 代理
    location /api {
        proxy_pass http://localhost:$BACKEND_PORT;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
    }
}
EOF

sudo ln -sf /etc/nginx/sites-available/c-project /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t && sudo systemctl reload nginx

echo "=========================================="
echo "  部署完成!"
echo "=========================================="
echo ""
echo "访问地址: http://$(curl -s ifconfig.me)"
echo "后端日志: tail -f $APP_DIR/backend/backend.log"
