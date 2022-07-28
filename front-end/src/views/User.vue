<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="clearfix">
              <span>基础信息</span>
            </div>
          </template>
          <div class="info">
            <div class="info-image" @click="showDialog">
              <img :src="avatarImg"/>
              <span class="info-edit">
                                <i class="el-icon-lx-camerafill"></i>
                            </span>
            </div>
            <div class="info-name">{{ name }}</div>
            <div class="info-desc">{{form.introduce}}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="clearfix">
              <span>账户编辑</span>
            </div>
          </template>
          <el-form label-width="90px" :rules="rules" ref="register" :model="form">
            <el-form-item label="用户名："> {{ name }}</el-form-item>
            <el-form-item label="旧密码：" prop="oldPwd">
              <el-input type="password" v-model="form.oldPwd"></el-input>
            </el-form-item>
            <el-form-item label="新密码：" prop="newPwd">
              <el-input type="password" v-model="form.newPwd"></el-input>
            </el-form-item>
            <el-form-item label="性别：">
              <el-radio-group v-model="form.gender" class="ml-4">
                <el-radio label="男" size="large">男</el-radio>
                <el-radio label="女" size="large">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="年龄：">
              <el-input type="number" v-model.number="form.age"></el-input>
            </el-form-item>
            <el-form-item label="邮箱：">
              <el-input  v-model="form.email"></el-input>
            </el-form-item>
            <el-form-item label="个人简介：">
              <el-input v-model="form.introduce"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="onSubmit">保存</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
    <el-dialog title="裁剪图片" v-model="dialogVisible" width="600px">
      <vue-cropper ref="cropper" :src="imgSrc" :ready="cropImage" :zoom="cropImage" :cropmove="cropImage"
                   style="width: 100%; height: 400px"></vue-cropper>

      <template #footer>
                <span class="dialog-footer">
                    <el-button class="crop-demo-btn" type="primary">选择图片
                        <input class="crop-input" type="file" name="image" accept="image/*" @change="setImage"/>
                    </el-button>
                    <el-button type="primary" @click="saveAvatar">上传并保存</el-button>
                </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {reactive, ref} from "vue";
import VueCropper from "vue-cropperjs";
import "cropperjs/dist/cropper.css";
import avatar from "../assets/img/admin.png";
import request from "../utils/request";
import {ElMessage} from "element-plus";

export default {
  name: "user",
  components: {
    VueCropper,
  },
  setup() {
    const name = localStorage.getItem("ms_username");
    const form = reactive({
      oldPwd: "",
      newPwd: "",
      gender:"",
      age:null,
      email:"",
      profile: "",//不可能！我的代码怎么可能会有bug！
      introduce:"",
      img:""
    });
    const rules = {
      oldPwd: [
        {
          required: true,
          message: "请输入旧密码",
          trigger: "blur",
        },
      ],
      newPwd: [
        { required: true, message: "请输入新密码", trigger: "blur" },
      ],
    };
    const userid = localStorage.getItem("ms_userid");//管理员id
    const getData = () => {
      request.get(`/admin/info/${userid}`).then(res => {
        if (res.code===455 ) {
          form.gender=res.data.userInfo.gender
          form.age=res.data.userInfo.age
          form.email=res.data.userInfo.email
          form.profile=res.data.userInfo.profile
          form.introduce=res.data.userInfo.introduce
          form.id=parseInt(userid)
          // avatarImg.value=res.data.img
        }
      })
    };
    getData();
    const register = ref(null);
    const onSubmit = () => {
      if (form.oldPwd!=""&&form.newPwd!=""){
      register.value.validate((valid) => {
        if (valid) {
          request.put("/admin/info", form).then(res => {
            if (res.code === 457) {
              ElMessage.success(`保存成功`);
              getData()
            } else {
              ElMessage.error(res.msg);
            }
          })
        }
      })
      }else{
        request.put("/admin/info", form).then(res => {
          if (res.code === 457) {
            ElMessage.success(`保存成功`);
            getData()
          } else {
            ElMessage.error(res.msg);
          }
        })
      }
    };

    const avatarImg = ref(avatar);
    const imgSrc = ref("");
    const cropImg = ref("");
    const dialogVisible = ref(false);
    const cropper = ref(null);

    const showDialog = () => {
      dialogVisible.value = true;
      imgSrc.value = avatarImg.value;
    };

    const setImage = (e) => {
      const file = e.target.files[0];
      if (!file.type.includes("image/")) {
        return;
      }
      const reader = new FileReader();
      reader.onload = (event) => {
        dialogVisible.value = true;
        imgSrc.value = event.target.result;
        cropper.value && cropper.value.replace(event.target.result);
      };
      reader.readAsDataURL(file);
    };

    const cropImage = () => {
      cropImg.value = cropper.value.getCroppedCanvas().toDataURL();
    };

    const saveAvatar = () => {
      avatarImg.value = cropImg.value;
      dialogVisible.value = false;
    };

    return {
      name,
      form,
      register,
      rules,
      onSubmit,
      cropper,
      avatarImg,
      imgSrc,
      cropImg,
      showDialog,
      dialogVisible,
      setImage,
      cropImage,
      saveAvatar,
    };
  },
};
</script>

<style scoped>
.info {
  text-align: center;
  padding: 35px 0;
}

.info-image {
  position: relative;
  margin: auto;
  width: 100px;
  height: 100px;
  background: #f8f8f8;
  border: 1px solid #eee;
  border-radius: 50px;
  overflow: hidden;
}

.info-image img {
  width: 100%;
  height: 100%;
}

.info-edit {
  display: flex;
  justify-content: center;
  align-items: center;
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.info-edit i {
  color: #eee;
  font-size: 25px;
}

.info-image:hover .info-edit {
  opacity: 1;
}

.info-name {
  margin: 15px 0 10px;
  font-size: 24px;
  font-weight: 500;
  color: #262626;
}

.crop-demo-btn {
  position: relative;
}

.crop-input {
  position: absolute;
  width: 100px;
  height: 40px;
  left: 0;
  top: 0;
  opacity: 0;
  cursor: pointer;
}

</style>
