drop database if exists spring_cloud_zuul;
create database spring_cloud_zuul;

use spring_cloud_zuul;
create table zuul_routes(
id int(12) auto_increment, /* 编号 */
path varchar(60) not null, /* 请求路径 */
service_id varchar(60) null, /* 服务编号 */
url varchar(200) null, /* 映射路径 */
`enable` bit not null default 1, /* 是否启用*/
retryable bit not null default 1, /* 使用支持重试*/
primary key(id)
);

insert into zuul_routes(path, service_id, url)
    values('/user-api/**', 'user', null);
insert into zuul_routes(path, service_id, url)
    values('/fund-api/**', null, 'http://localhost:7001');