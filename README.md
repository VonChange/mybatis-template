mybatis 语法模板语言

#### 依赖于mybatis 

> 只是使用了mybatis 模版引擎动态sql这部分功能 在此基础上做了扩展
 
####主要方法 com.vonchange.mybatis.tpl.MybatisTpl
> public static SqlWithParam generate(String sqlInXml,
> Map<String,Object> parameter) 支持mybatis语法 返回带?号sql语句和参数


> 偷懒简化 if test 和in查询 识别 {@开头 和原来mybatis写法可混用


1. {@and id in idList} 等于
 
 ```
<if test="null!=idList and idList.size>0">
  and id in <foreach collection="idList" index="index" item="item"
  open="(" separator="," close=")">#{item}</foreach></if> 
```
  
2. {@and user_name <> userName} 等于 

```
<if test="null!=userName and ''!=userName"> and user_name <>
#{userName} </if>
```
   
3. in 查询List实体下的属性 {@and id in userList:id} 