#!/bin/bash
# Ubuntu 环境搭建脚本 - C语言数据文件智能处理平台
# 适用于 Ubuntu 20.04/22.04

set -e

echo "=========================================="
echo "  C语言数据文件智能处理平台 - 环境搭建"
echo "=========================================="

# 更新系统
echo "[1/6] 更新系统包..."
sudo apt update && sudo apt upgrade -y

# 安装基础工具
echo "[2/6] 安装基础工具..."
sudo apt install -y curl wget git unzip vim

# 安装 JDK 17
echo "[3/6] 安装 JDK 17..."
sudo apt install -y openjdk-17-jdk
java -version

# 设置 JAVA_HOME
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc

# 安装 Maven
echo "[4/6] 安装 Maven..."
sudo apt install -y maven
mvn -version

# 安装 Node.js 18
echo "[5/6] 安装 Node.js 18..."
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs
node -v
npm -v

# 安装 nginx (可选，用于生产环境)
echo "[6/6] 安装 Nginx..."
sudo apt install -y nginx

echo "=========================================="
echo "  环境安装完成!"
echo "=========================================="
echo ""
echo "版本信息:"
echo "  Java: $(java -version 2>&1 | head -1)"
echo "  Maven: $(mvn -version 2>&1 | head -1)"
echo "  Node.js: $(node -v)"
echo "  npm: $(npm -v)"
echo ""
echo "下一步: 运行 ./deploy-app.sh 部署应用"
