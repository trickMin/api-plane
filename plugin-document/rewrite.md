| 字段            | 含义    | 范围                  | 备注 |    |
|:--------------|:------|:--------------------|:---|:---|
| kind          | 插件类型  | rewrite |    |    |
| action.rewrite_regex | 提取group | | | |
| action.target | 重写目标路径 | | |

```
场景1：重写path
{
  "kind": "rewrite",
  "action": {
    "rewrite_regex": "/rewrite(.*)",
    "target": "/anything$1"
  }
}
```