package spring.cloud.fund.facade;

//@FeignClient(value = "user")
// 此处继承UserFacade，那么OpenFeign定义的方法也能继承下来
public interface UserClient extends UserFacade {
}
