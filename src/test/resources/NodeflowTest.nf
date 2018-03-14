{
 "indexs":[{"idx":"foo","type":"INT"},{"idx":"bar","mode":2,"type":"INT"}],
 "nodes":[
   {"id":"n1","component":"ruleset","ruleset":["bar=foo*1000","bar+=24"],"next":"n2"},
   {"id":"n2","next":[{"condition":"bar==1024",next:"n3"},{"condition":"bar==2024","next":"n4"},{"next":"n5"}]},
   {"id":"n3","component":"testCustomComponent"},
   {"id":"n4","component":"delay", "delay":3},
   {"id":"n5"}
 ]
}