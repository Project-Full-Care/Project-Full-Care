import { useMutation, useQueryClient } from "react-query";
import { toast } from "react-toastify";

import {
  addLikeRecruitmentPost,
  addRecruitmentPost,
} from "../lib/apis/memberRecruitmentApi";

// 특정 모집글 좋아요
export const useAddLikeRecruitmentMutation = () => {
  const queryClient = useQueryClient();

  return useMutation(addLikeRecruitmentPost, {
    onSuccess: () => {
      queryClient.invalidateQueries("recruitmentDetail");
    },
    onError: (err) => {
      toast.error(err);
    },
  });
};

// 모집글 생성
export const useAddRecruitmentPostMutation = () => {
  const queryClient = useQueryClient();

  return useMutation(addRecruitmentPost, {
    onSuccess: () => {
      toast.success("모집글이 생성되었습니다.");
      queryClient.invalidateQueries("allRecruitmentPosts");
    },
  });
};
