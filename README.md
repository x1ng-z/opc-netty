# 工程简介
基于netty的opc数据服务

工具类在starter包下
调用顺序如下
##1、OpcConnectServeOperator类:opc服务注册等操作
##2、OpcItemOperator类:opc点号操作
##3、OpcDataOperator类:opc已注册点号数据读写操作
具体操作所需的类说明，查看具体bean类内字段说明


运行说明：
将opcproxy.exe、msvcp140.dll、vcruntime140.dll三个文件放置于该工程(opc-netty)打包完的jar同一目录下