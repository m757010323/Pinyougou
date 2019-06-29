app.controller("baseController",function ($scope) {
    //重新加载列表 数据
    $scope.reloadList=function(){
        //切换页码
        // $scope.findPage( $scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        //由于查询更改重新加载数据的方法
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    }

    //分页控件配置 currentPage:当前页   totalItems:总记录数  itemsPerPage:每页记录数 perPageOptions:分页选项 onchange:当页码变更时,自动触发方法
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function(){
            $scope.reloadList();//重新加载
        }
    };

    $scope.selectIds = [];//选中的ID集合
    //更新复选
    $scope.updateSelectction = function ($event,id) {

        if($event.target.checked){//如果被选中则添加到数组中
            $scope.selectIds.push(id);
        }else{
            var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx,1);//删除
        }
    }


});