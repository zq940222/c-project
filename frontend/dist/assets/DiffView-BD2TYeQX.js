import{M as z}from"./MonacoEditor-BkwIVYFS.js";import{d as j}from"./index-CEDoj4eB.js";import{_ as q,r as k,a as D,b as a,g as w,i as _,w as t,e as r,o as c,f as o,t as s,F as L,m as G,j as b}from"./index-DIi-ZwBs.js";const H={class:"diff-view"},J={key:0},K={class:"diff-output"},P=`// 版本1 - 原始数据
struct 猫 {
    char* ID;
    char* 名字;
    char* 品种;
    int 出生日期;
    char* 颜色;
};

猫 园区的猫们[] = {
    {"CAT001", "小黄", "橘猫", 2023, "黄色"},
    {"CAT002", "小黑", "狸花", 2024, "灰色"},
    {"CAT003", "小白", "波斯", 2022, "白色"}
};

int count = 0x01;
`,Q=`// 版本2 - 修改后数据
struct 猫 {
    char* ID;
    char* 名字;
    char* 品种;
    int 出生日期;
    char* 颜色;
};

猫 园区的猫们[] = {
    {"CAT001", "小黄", "金渐层", 2023, "黄色"},
    {"CAT003", "小白", "波斯", 2022, "白色"},
    {"CAT002", "小黑", "狸花", 2024, "灰色"},
    {"CAT004", "花花", "狸花", 2023, "花色"}
};

int count = 0x1;
`,X={__name:"DiffView",setup(Z){const g=k(""),v=k(""),n=k(null),x=k(!1),i=k({ignoreWhitespace:!0,ignoreComments:!0,ignoreOrder:!0,ignoreNumberFormat:!0,semanticCompare:!0}),N=[{title:"类型",key:"type",width:100},{title:"位置",key:"location",width:100},{title:"左侧内容",dataIndex:"leftContent",key:"leftContent",ellipsis:!0},{title:"右侧内容",dataIndex:"rightContent",key:"rightContent",ellipsis:!0},{title:"可忽略",key:"ignorable",width:120}];function R(){g.value=P}function F(){v.value=Q}async function I(){if(!g.value.trim()||!v.value.trim()){b.warning("请输入两个文件的内容");return}x.value=!0;try{const u=await j.compare(g.value,v.value,i.value);u.code===200?(n.value=u.data,b.success("比对完成")):b.error(u.message||"比对失败")}catch{b.error("比对失败")}finally{x.value=!1}}function M(u){switch(u){case"ADDED":return"green";case"REMOVED":return"red";case"MODIFIED":return"blue";case"FORMAT_ONLY":return"cyan";case"ORDER_ONLY":return"purple";default:return"default"}}function U(u){switch(u){case"ADDED":return"新增";case"REMOVED":return"删除";case"MODIFIED":return"修改";case"FORMAT_ONLY":return"格式差异";case"ORDER_ONLY":return"顺序差异";default:return u}}return(u,l)=>{const h=r("a-button"),f=r("a-card"),m=r("a-col"),O=r("a-row"),y=r("a-checkbox"),V=r("a-space"),p=r("a-tag"),S=r("a-table"),T=r("a-statistic"),B=r("a-statistic-card-group"),Y=r("a-divider"),C=r("a-descriptions-item"),W=r("a-descriptions");return c(),D("div",H,[a(O,{gutter:16},{default:t(()=>[a(m,{span:12},{default:t(()=>[a(f,{title:"左侧文件 (原始版本)",bordered:!1,size:"small"},{extra:t(()=>[a(h,{size:"small",onClick:R},{default:t(()=>[...l[7]||(l[7]=[o("加载示例",-1)])]),_:1})]),default:t(()=>[a(z,{modelValue:g.value,"onUpdate:modelValue":l[0]||(l[0]=e=>g.value=e),language:"c",height:"350px"},null,8,["modelValue"])]),_:1})]),_:1}),a(m,{span:12},{default:t(()=>[a(f,{title:"右侧文件 (新版本)",bordered:!1,size:"small"},{extra:t(()=>[a(h,{size:"small",onClick:F},{default:t(()=>[...l[8]||(l[8]=[o("加载示例",-1)])]),_:1})]),default:t(()=>[a(z,{modelValue:v.value,"onUpdate:modelValue":l[1]||(l[1]=e=>v.value=e),language:"c",height:"350px"},null,8,["modelValue"])]),_:1})]),_:1})]),_:1}),a(f,{title:"比对选项",bordered:!1,style:{"margin-top":"16px"},size:"small"},{default:t(()=>[a(V,null,{default:t(()=>[a(y,{checked:i.value.ignoreWhitespace,"onUpdate:checked":l[2]||(l[2]=e=>i.value.ignoreWhitespace=e)},{default:t(()=>[...l[9]||(l[9]=[o("忽略空白差异",-1)])]),_:1},8,["checked"]),a(y,{checked:i.value.ignoreComments,"onUpdate:checked":l[3]||(l[3]=e=>i.value.ignoreComments=e)},{default:t(()=>[...l[10]||(l[10]=[o("忽略注释差异",-1)])]),_:1},8,["checked"]),a(y,{checked:i.value.ignoreOrder,"onUpdate:checked":l[4]||(l[4]=e=>i.value.ignoreOrder=e)},{default:t(()=>[...l[11]||(l[11]=[o("忽略顺序差异",-1)])]),_:1},8,["checked"]),a(y,{checked:i.value.ignoreNumberFormat,"onUpdate:checked":l[5]||(l[5]=e=>i.value.ignoreNumberFormat=e)},{default:t(()=>[...l[12]||(l[12]=[o("忽略数值格式差异",-1)])]),_:1},8,["checked"]),a(y,{checked:i.value.semanticCompare,"onUpdate:checked":l[6]||(l[6]=e=>i.value.semanticCompare=e)},{default:t(()=>[...l[13]||(l[13]=[o("启用语义比对",-1)])]),_:1},8,["checked"]),a(h,{type:"primary",onClick:I,loading:x.value},{default:t(()=>[...l[14]||(l[14]=[o(" 执行比对 ",-1)])]),_:1},8,["loading"])]),_:1})]),_:1}),n.value?(c(),w(f,{key:0,title:"比对结果",bordered:!1,style:{"margin-top":"16px"}},{extra:t(()=>[a(V,null,{default:t(()=>[a(p,{color:"red"},{default:t(()=>{var e;return[o("新增: "+s(((e=n.value.summary)==null?void 0:e.added)||0),1)]}),_:1}),a(p,{color:"orange"},{default:t(()=>{var e;return[o("删除: "+s(((e=n.value.summary)==null?void 0:e.removed)||0),1)]}),_:1}),a(p,{color:"blue"},{default:t(()=>{var e;return[o("修改: "+s(((e=n.value.summary)==null?void 0:e.modified)||0),1)]}),_:1}),a(p,{color:"green"},{default:t(()=>{var e;return[o("可忽略: "+s(((e=n.value.summary)==null?void 0:e.ignorableDiffs)||0),1)]}),_:1})]),_:1})]),default:t(()=>[a(O,{gutter:16},{default:t(()=>[a(m,{span:16},{default:t(()=>[a(f,{title:"差异详情",size:"small"},{default:t(()=>[a(S,{columns:N,"data-source":n.value.items,pagination:{pageSize:10},size:"small"},{bodyCell:t(({column:e,record:d})=>{var A,E;return[e.key==="type"?(c(),w(p,{key:0,color:M(d.type)},{default:t(()=>[o(s(U(d.type)),1)]),_:2},1032,["color"])):_("",!0),e.key==="location"?(c(),D(L,{key:1},[d.leftLocation||d.rightLocation?(c(),D("span",J,s(((A=d.leftLocation)==null?void 0:A.startLine)||"-")+" → "+s(((E=d.rightLocation)==null?void 0:E.startLine)||"-"),1)):_("",!0)],64)):_("",!0),e.key==="ignorable"?(c(),D(L,{key:2},[d.ignorable?(c(),w(p,{key:0,color:"green"},{default:t(()=>[o(s(d.ignoreReason||"可忽略"),1)]),_:2},1024)):_("",!0)],64)):_("",!0)]}),_:1},8,["data-source"])]),_:1})]),_:1}),a(m,{span:8},{default:t(()=>[a(f,{title:"比对摘要",size:"small"},{default:t(()=>[a(B,null,{default:t(()=>[a(O,{gutter:16},{default:t(()=>[a(m,{span:12},{default:t(()=>{var e;return[a(T,{title:"总差异数",value:((e=n.value.summary)==null?void 0:e.totalDiffs)||0},null,8,["value"])]}),_:1}),a(m,{span:12},{default:t(()=>{var e;return[a(T,{title:"实质差异",value:((e=n.value.summary)==null?void 0:e.significantDiffs)||0,"value-style":{color:"#cf1322"}},null,8,["value"])]}),_:1})]),_:1})]),_:1}),a(Y),a(W,{column:1,size:"small"},{default:t(()=>[a(C,{label:"新增"},{default:t(()=>{var e;return[o(s(((e=n.value.summary)==null?void 0:e.added)||0)+" 处 ",1)]}),_:1}),a(C,{label:"删除"},{default:t(()=>{var e;return[o(s(((e=n.value.summary)==null?void 0:e.removed)||0)+" 处 ",1)]}),_:1}),a(C,{label:"修改"},{default:t(()=>{var e;return[o(s(((e=n.value.summary)==null?void 0:e.modified)||0)+" 处 ",1)]}),_:1}),a(C,{label:"可忽略"},{default:t(()=>{var e;return[o(s(((e=n.value.summary)==null?void 0:e.ignorableDiffs)||0)+" 处 ",1)]}),_:1})]),_:1})]),_:1})]),_:1})]),_:1}),a(f,{title:"Unified Diff",size:"small",style:{"margin-top":"16px"}},{default:t(()=>[G("pre",K,s(n.value.unifiedDiff),1)]),_:1})]),_:1})):_("",!0)])}}},ae=q(X,[["__scopeId","data-v-ac381621"]]);export{ae as default};
