app.controller('brandController',function ($scope,$controller,brandService) {

    $controller('baseController',{$scope:$scope});//继承

    $scope.findAll=function () {
        brandService.findAll().success(
            function (response) {
            $scope.list = response;
        });
    }

    // //重新加载列表 数据
    // $scope.reloadList=function(){
    //     //切换页码
    //     // $scope.findPage( $scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    //     //由于查询更改重新加载数据的方法
    //     $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    // }


    //分页控件配置 currentPage:当前页   totalItems:总记录数  itemsPerPage:每页记录数 perPageOptions:分页选项 onchange:当页码变更时,自动触发方法
    // $scope.paginationConf = {
    //     currentPage: 1,
    //     totalItems: 10,
    //     itemsPerPage: 10,
    //     perPageOptions: [10, 20, 30, 40, 50],
    //     onChange: function(){
    //         $scope.reloadList();//重新加载
    //     }
    // };
    //分页
    $scope.findPage =function (page,rows) {
        brandService.findPage(page,rows).success(function (response) {
            $scope.list = response.rows;
            $scope.paginationConf.totalItems = response.total;//更新总记录数
        })
    }

    //添加
    $scope.add = function () {

        // var methodName = "add";
        var object = null;
        if($scope.entity.id != null){
            object=brandService.update($scope.entity);
        }else{
            object=brandService.add($scope.entity);
        }
        object.success(function (response) {
            if(response.success){
                $scope.reloadList();
            }
            else{
                alert(response.message);
            }
        })

    }

    //查询实体
    $scope.findOne = function (id) {
        brandService.findOne(id).success(function (response) {
            $scope.entity = response;
        })
    }

    // $scope.selectIds = [];//选中的ID集合
    // //更新复选
    // $scope.updateSelectction = function ($event,id) {
    //
    //     if($event.target.checked){//如果被选中则添加到数组中
    //         $scope.selectIds.push(id);
    //     }else{
    //         var idx = $scope.selectIds.indexOf(id);
    //         $scope.selectIds.splice(idx,1);//删除
    //     }
    // }

    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        brandService.dele($scope.selectIds).success(function (response) {
            if(response.success){
                $scope.reloadList();
            }
        })
    }

    $scope.searchEntity = {};//定义搜索对象

    //条件查询
    $scope.search = function (page,rows) {
        brandService.search(page,rows,$scope.searchEntity).success(
            function (response) {
                $scope.paginationConf.totalItems = response.total;
                $scope.list =response.rows;

            }
        );
    }



});