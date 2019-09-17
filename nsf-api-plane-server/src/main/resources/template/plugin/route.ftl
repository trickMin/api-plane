#@type=pluginSchema
#@version=1.0
#@description=路由插件schema
{
    "fields": {
        "rule": {
            "type": "array",
            "alias": "规则列表",
            "required": false,
            "item_schema": {
                "fields": {
                    "name": {
                        "type": "string"
                    },
                    "matcher": {
                        "type": "array",
                        "alias": "条件列表",
                        "required": false,
                        "item_schema": {
                            "fields": {
                                "source_type": {
                                    "type": "string",
                                    "alias": "类型",
                                    "enum": [
                                        "Header",
                                        "URI",
                                        "Host",
                                        "User-Agent",
                                        "Args",
                                        "Cookie"
                                    ]
                                },
                                "left_value": {
                                    "type": "string",
                                    "alias": "只有Header、Cookie和Args有此参数，代表name"
                                },
                                "op": {
                                    "type": "string",
                                    "enum": [
                                        "=",
                                        "!=",
                                        "startsWith",
                                        "endsWith",
                                        "regex",
                                        "nonRegex"
                                    ]
                                },
                                "right_value": {
                                    "type": "string"
                                }
                            }
                        }
                    },
                    "action": {
                        "type": "table",
                        "alias": "动作",
                        "required": true,
                        "item_schema": {
                            "fields": {
                                "action_type": {
                                    "type": "string",
                                    "enum": [
                                        "rewrite",
                                        "pass_proxy",
                                        "redirect",
                                        "return"
                                    ],
                                    "required": true
                                },
                                "rewrite_regex": {
                                    "type": "string",
                                    "alias": "正则。仅rewrite有用",
                                    "required": false
                                },
                                "redirect_type": {
                                    "type": "number",
                                    "enum": [
                                        301,
                                        302
                                    ]
                                },
                                "target": {
                                    "type": "string",
                                    "alias": "目标路径。仅rewrite、redirect有用",
                                    "required": false
                                },
                                "pass_proxy_target": {
                                    "type": "array",
                                    "alias": "仅pass_proxy有用",
                                    "item_schema": {
                                        "fields": {
                                            "url": {
                                                "type": "string"
                                            },
                                            "weight": {
                                                "type": "number",
                                                "alias": "权重"
                                            }
                                        }
                                    }
                                },
                                "return_target": {
                                    "type": "table",
                                    "alias": "仅类型为return时有用",
                                    "item_schema": {
                                        "fields": {
                                            "code": {
                                                "type": "number",
                                                "alias": "状态码"
                                            },
                                            "header": {
                                                "type": "array",
                                                "item_schema": {
                                                    "name": {
                                                        "type": "string"
                                                    },
                                                    "value": {
                                                        "type": "string"
                                                    }
                                                }
                                            },
                                            "body": {
                                                "type": "string"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
---
#@type=istioResource
#@version=1.0
#@processor=RouteProcessor
{}
---