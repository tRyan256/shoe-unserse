import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link, useLocation } from 'react-router-dom';
import { ArrowLeft, Heart, MessageCircle, Share2, Send, Loader2, X, ChevronLeft, ChevronRight, MoreHorizontal, Pencil, Trash2, Eye, EyeOff } from 'lucide-react';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Dialog, DialogContent, DialogDescription } from '@/components/ui/dialog';
import { UserCard } from '@/components/shared';
import { experienceApi } from '@/api';
import { useUserStore } from '@/stores';
import { toast } from 'sonner';
import type { ExperiencePostDetail, ExperienceComment, ExperienceReply } from '@/types';

const SCROLL_KEY = 'community_scroll_position';
const POSTS_KEY = 'community_posts_cache';
const STATE_KEY = 'community_state_cache';
const FROM_COMMUNITY_DETAIL_KEY = 'from_community_detail';

export default function CommunityDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const isLoggedIn = useUserStore((state) => state.isLoggedIn);
  const currentUserId = useUserStore((state) => state.user?.id);

  const [post, setPost] = useState<ExperiencePostDetail | null>(null);
  const [comments, setComments] = useState<ExperienceComment[]>([]);
  const [loading, setLoading] = useState(true);
  const [commentsLoading, setCommentsLoading] = useState(false);
  const [commentPage, setCommentPage] = useState(1);
  const [hasMoreComments, setHasMoreComments] = useState(true);
  const [totalComments, setTotalComments] = useState(0);

  const [isLiked, setIsLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(0);
  const [isFollowed, setIsFollowed] = useState(false);

  const [commentText, setCommentText] = useState('');
  const [replyingTo, setReplyingTo] = useState<{
    commentId: number;        // 所属评论ID
    userId: number;           // 被回复者ID
    userName: string;         // 被回复者昵称
    replyId?: number;         // 被回复的回复ID（回复回复时传入）
    isReplyToReply: boolean;  // 是否是回复回复（用于UI显示）
  } | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const [previewOpen, setPreviewOpen] = useState(false);
  const [previewIndex, setPreviewIndex] = useState(0);

  const [showActionMenu, setShowActionMenu] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [showHideConfirm, setShowHideConfirm] = useState(false);
  const [isHidden, setIsHidden] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [hiding, setHiding] = useState(false);

  const [editingComment, setEditingComment] = useState<ExperienceComment | null>(null);
  const [deletingComment, setDeletingComment] = useState<ExperienceComment | null>(null);
  const [editingReply, setEditingReply] = useState<ExperienceReply | null>(null);
  const [deletingReply, setDeletingReply] = useState<ExperienceReply | null>(null);
  const [editContent, setEditContent] = useState('');

  useEffect(() => {
    if (id) {
      fetchPostDetail();
      fetchComments(1);
    }
  }, [id]);

  const fetchPostDetail = async () => {
    try {
      setLoading(true);
      const data = await experienceApi.getPostDetail(Number(id));
      setPost(data);
      setIsLiked(data.isLiked);
      setLikeCount(data.likeCount);
      setIsFollowed(data.isFollowed);
      setIsHidden(data.hidden === 1);
    } catch {
      toast.error('加载失败');
      navigate('/community');
    } finally {
      setLoading(false);
    }
  };

  const fetchComments = async (page: number) => {
    try {
      setCommentsLoading(true);
      const result = await experienceApi.listComments(Number(id), { page, size: 20 });
      if (page === 1) {
        setComments(result.records);
      } else {
        setComments((prev) => [...prev, ...result.records]);
      }
      setTotalComments(result.total);
      setHasMoreComments(result.records.length === 20);
    } catch {
      console.error('Failed to fetch comments:');
    } finally {
      setCommentsLoading(false);
    }
  };

  const handleLoadMoreComments = () => {
    if (!commentsLoading && hasMoreComments) {
      const nextPage = commentPage + 1;
      setCommentPage(nextPage);
      fetchComments(nextPage);
    }
  };

  const handleLike = async () => {
    if (!isLoggedIn()) {
      toast.info('请先登录');
      return;
    }

    try {
      if (isLiked) {
        await experienceApi.unlikePost(Number(id));
        setIsLiked(false);
        setLikeCount((prev) => Math.max(0, prev - 1));
      } else {
        await experienceApi.likePost(Number(id));
        setIsLiked(true);
        setLikeCount((prev) => prev + 1);
      }
    } catch {
      toast.error('操作失败');
    }
  };

  const handleFollow = async () => {
    if (!isLoggedIn()) {
      toast.info('请先登录');
      return;
    }

    if (!post) return;

    try {
      if (isFollowed) {
        await experienceApi.unfollowUser(post.userId);
        setIsFollowed(false);
        toast.success('已取消关注');
      } else {
        await experienceApi.followUser(post.userId);
        setIsFollowed(true);
        toast.success('关注成功');
      }
    } catch {
      toast.error('操作失败');
    }
  };

  const handleSubmitComment = async () => {
    if (!isLoggedIn()) {
      toast.info('请先登录');
      return;
    }

    const text = commentText.trim();
    if (!text) {
      toast.info('请输入内容');
      return;
    }

    if (!post) return;

    setSubmitting(true);
    try {
      if (replyingTo) {
        // 回复评论或回复回复
        await experienceApi.replyComment(replyingTo.commentId, {
          commentId: replyingTo.commentId,
          content: text,
          targetUserId: replyingTo.userId,
          targetReplyId: replyingTo.replyId,  // 回复回复时传入
        });
        toast.success('回复成功');
      } else {
        await experienceApi.createComment({
          postId: post.id,
          content: text,
        });
        toast.success('评论成功');
      }
      setCommentText('');
      setReplyingTo(null);
      setCommentPage(1);
      fetchComments(1);
    } catch {
      toast.error('发送失败');
    } finally {
      setSubmitting(false);
    }
  };

  const handleLikeComment = async (commentId: number, isLiked: boolean) => {
    if (!isLoggedIn()) {
      toast.info('请先登录');
      return;
    }

    try {
      if (isLiked) {
        await experienceApi.unlikeComment(commentId);
      } else {
        await experienceApi.likeComment(commentId);
      }
      
      setComments((prev) =>
        prev.map((c) => {
          if (c.id === commentId) {
            return {
              ...c,
              isLiked: !isLiked,
              likeCount: isLiked ? c.likeCount - 1 : c.likeCount + 1,
            };
          }
          if (c.replies) {
            const updatedReplies = c.replies.map((r) =>
              r.id === commentId
                ? {
                    ...r,
                    isLiked: !isLiked,
                    likeCount: isLiked ? r.likeCount - 1 : r.likeCount + 1,
                  }
                : r
            );
            return { ...c, replies: updatedReplies };
          }
          return c;
        })
      );
    } catch (error: unknown) {
      const err = error as { response?: { status?: number } };
      if (err.response?.status === 409) {
        fetchComments(1);
        toast.info('请勿重复操作');
      } else {
        toast.error('操作失败');
      }
    }
  };

  const handleDelete = async () => {
    if (!post) return;
    
    setDeleting(true);
    try {
      await experienceApi.deletePost(post.id);
      toast.success('删除成功');
      setShowDeleteConfirm(false);
      navigate('/community');
    } catch {
      toast.error('删除失败');
    } finally {
      setDeleting(false);
    }
  };

  const handleHide = async () => {
    if (!post) return;
    
    setHiding(true);
    try {
      if (isHidden) {
        await experienceApi.unhidePost(post.id);
        setIsHidden(false);
        setPost(prev => prev ? { ...prev, hidden: 0 } : null);
        toast.success('已取消隐藏');
      } else {
        await experienceApi.hidePost(post.id);
        setIsHidden(true);
        setPost(prev => prev ? { ...prev, hidden: 1 } : null);
        toast.success('已隐藏');
      }
      setShowHideConfirm(false);
    } catch {
      toast.error('操作失败');
    } finally {
      setHiding(false);
    }
  };

  const handleEditComment = async () => {
    if (!editingComment || !editContent.trim()) return;
    
    try {
      await experienceApi.updateComment(editingComment.id, editContent.trim());
      toast.success('修改成功');
      setEditingComment(null);
      setEditContent('');
      fetchComments(1);
    } catch {
      toast.error('修改失败');
    }
  };

  const handleDeleteComment = async () => {
    if (!deletingComment) return;
    
    try {
      await experienceApi.deleteComment(deletingComment.id);
      toast.success('删除成功');
      setDeletingComment(null);
      fetchComments(1);
    } catch {
      toast.error('删除失败');
    }
  };

  const handleHideComment = async (comment: ExperienceComment) => {
    try {
      await experienceApi.hideOwnComment(comment.id);
      toast.success('已隐藏');
      fetchComments(1);
    } catch {
      toast.error('操作失败');
    }
  };

  const handleUnhideComment = async (comment: ExperienceComment) => {
    try {
      await experienceApi.unhideOwnComment(comment.id);
      toast.success('已取消隐藏');
      fetchComments(1);
    } catch {
      toast.error('操作失败');
    }
  };

  const handleEditReply = async () => {
    if (!editingReply || !editContent.trim()) return;
    
    try {
      await experienceApi.updateReply(editingReply.id, editContent.trim());
      toast.success('修改成功');
      setEditingReply(null);
      setEditContent('');
      fetchComments(1);
    } catch {
      toast.error('修改失败');
    }
  };

  const handleDeleteReply = async () => {
    if (!deletingReply) return;
    
    try {
      await experienceApi.deleteReply(deletingReply.id);
      toast.success('删除成功');
      setDeletingReply(null);
      fetchComments(1);
    } catch {
      toast.error('删除失败');
    }
  };

  const handleHideReply = async (reply: ExperienceReply) => {
    try {
      await experienceApi.hideOwnReply(reply.id);
      toast.success('已隐藏');
      fetchComments(1);
    } catch {
      toast.error('操作失败');
    }
  };

  const handleUnhideReply = async (reply: ExperienceReply) => {
    try {
      await experienceApi.unhideOwnReply(reply.id);
      toast.success('已取消隐藏');
      fetchComments(1);
    } catch {
      toast.error('操作失败');
    }
  };

  const formatTime = (time: string) => {
    const date = new Date(time);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return '刚刚';
    if (minutes < 60) return `${minutes}分钟前`;
    if (hours < 24) return `${hours}小时前`;
    if (days < 7) return `${days}天前`;
    return date.toLocaleDateString('zh-CN');
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <Loader2 className="w-8 h-8 text-emerald-600 animate-spin" />
      </div>
    );
  }

  if (!post) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50 pb-24">
      <header className="sticky top-0 z-40 bg-white border-b border-gray-100">
        <div className="flex items-center justify-between h-12 px-4">
          <button onClick={() => navigate('/community', { state: { fromDetail: true } })} className="p-1.5 -ml-1.5">
            <ArrowLeft className="w-5 h-5" />
          </button>
          <span className="text-sm font-medium">心得详情</span>
          <div className="flex items-center gap-1">
            {post && post.userId === Number(currentUserId) && (
              <div className="relative">
                <button 
                  onClick={() => setShowActionMenu(!showActionMenu)}
                  className="p-1.5 -mr-1.5"
                >
                  <MoreHorizontal className="w-5 h-5" />
                </button>
                {showActionMenu && (
                  <>
                    <div 
                      className="fixed inset-0 z-40" 
                      onClick={() => setShowActionMenu(false)}
                    />
                    <div className="absolute right-0 top-full mt-1 w-28 bg-white rounded-lg shadow-lg border border-gray-100 z-50 overflow-hidden">
                      <button
                        onClick={() => {
                          setShowActionMenu(false);
                          navigate(`/community/edit/${post.id}`);
                        }}
                        className="w-full flex items-center gap-1.5 px-2.5 py-2 text-xs text-gray-700 hover:bg-gray-50"
                      >
                        <Pencil className="w-3.5 h-3.5" />
                        编辑
                      </button>
                      <button
                        onClick={() => {
                          setShowActionMenu(false);
                          setShowHideConfirm(true);
                        }}
                        className="w-full flex items-center gap-1.5 px-2.5 py-2 text-xs text-gray-700 hover:bg-gray-50"
                      >
                        {isHidden ? (
                          <>
                            <Eye className="w-3.5 h-3.5" />
                            取消隐藏
                          </>
                        ) : (
                          <>
                            <EyeOff className="w-3.5 h-3.5" />
                            隐藏
                          </>
                        )}
                      </button>
                      <button
                        onClick={() => {
                          setShowActionMenu(false);
                          setShowDeleteConfirm(true);
                        }}
                        className="w-full flex items-center gap-1.5 px-2.5 py-2 text-xs text-red-500 hover:bg-red-50"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                        删除
                      </button>
                    </div>
                  </>
                )}
              </div>
            )}
            <button className="p-1.5">
              <Share2 className="w-4 h-4" />
            </button>
          </div>
        </div>
      </header>

      <div className="bg-white">
        <div className="p-3">
          <div className="flex items-center gap-2.5 mb-3">
            <UserCard userId={post.userId}>
              <Avatar className="w-9 h-9">
                <AvatarImage src={post.userAvatar} />
                <AvatarFallback>{post.userName?.[0]}</AvatarFallback>
              </Avatar>
            </UserCard>
            <div className="flex-1">
              <p className="text-sm font-medium text-gray-800">{post.userName}</p>
              <p className="text-xs text-gray-400">{formatTime(post.createTime)}</p>
            </div>
            {post.userId !== Number(currentUserId) && (
              <button
                onClick={handleFollow}
                className={`px-3 py-1 text-xs rounded-full ${
                  isFollowed
                    ? 'bg-gray-100 text-gray-500'
                    : 'bg-emerald-600 text-white'
                }`}
              >
                {isFollowed ? '已关注' : '关注'}
              </button>
            )}
          </div>

          {post.hidden === 1 && (
            <div className="flex items-center gap-1 text-xs text-gray-400 mb-3 bg-gray-100 rounded-full px-2 py-1 w-fit">
              <EyeOff className="w-3 h-3" />
              <span>仅自己可见</span>
            </div>
          )}

          <p className="text-sm text-gray-800 leading-relaxed mb-3">{post.content}</p>

          {post.images && post.images.length > 0 && (
            <div className={`grid gap-1.5 mb-3 ${
              post.images.length === 1 ? 'grid-cols-1' :
              post.images.length === 2 ? 'grid-cols-2' :
              'grid-cols-3'
            }`}>
              {post.images.map((img, index) => (
                <img
                  key={index}
                  src={img}
                  alt=""
                  className="w-full aspect-square object-cover rounded-lg cursor-pointer"
                  onClick={() => {
                    setPreviewIndex(index);
                    setPreviewOpen(true);
                  }}
                />
              ))}
            </div>
          )}

          {post.productName && (
            <Link
              to={post.productType === 1 ? `/shoe/${post.productId}` : `/bundle/${post.productId}`}
              onClick={() => {
                sessionStorage.setItem(FROM_COMMUNITY_DETAIL_KEY, 'true');
              }}
              className="flex items-center gap-2 bg-gray-50 rounded-lg p-2 mb-3"
            >
              <img
                src={post.productImage || '/placeholder.png'}
                alt={post.productName}
                className="w-12 h-12 rounded-lg object-cover"
              />
              <div className="flex-1">
                <p className="text-xs font-medium text-gray-800">{post.productName}</p>
                <p className="text-[10px] text-gray-400 mt-0.5">点击查看商品详情</p>
              </div>
            </Link>
          )}

          <div className="flex items-center gap-5 pt-2 border-t border-gray-100">
            <button
              onClick={handleLike}
              className="flex items-center gap-1.5 text-gray-500"
            >
              <Heart
                className={`w-5 h-5 transition-all ${
                  isLiked ? 'fill-red-500 text-red-500' : ''
                }`}
              />
              <span className="text-xs">{likeCount}</span>
            </button>
            <button
              onClick={() => {
                if (!isLoggedIn()) {
                  toast.info('请先登录');
                  return;
                }
                (document.querySelector('input[placeholder*="说点什么"]') as HTMLInputElement)?.focus();
              }}
              className="flex items-center gap-1.5 text-gray-500"
            >
              <MessageCircle className="w-5 h-5" />
              <span className="text-xs">{totalComments}</span>
            </button>
          </div>
        </div>
      </div>

      <div className="bg-white mt-2">
        <div className="p-3 border-b border-gray-100">
          <p className="text-sm font-medium">评论 ({totalComments})</p>
        </div>

        {comments.length === 0 && !commentsLoading && (
          <div className="py-10 text-center text-gray-400">
            <MessageCircle className="w-10 h-10 mx-auto mb-2 opacity-50" />
            <p className="text-xs">暂无评论，来抢沙发吧</p>
          </div>
        )}

        <div className="divide-y divide-gray-50">
          {comments.map((comment) => (
            <div key={comment.id} className="p-3">
              <div className="flex gap-2.5">
                <UserCard userId={comment.userId}>
                  <Avatar className="w-8 h-8 flex-shrink-0">
                    <AvatarImage src={comment.userAvatar} />
                    <AvatarFallback>{comment.userName?.[0]}</AvatarFallback>
                  </Avatar>
                </UserCard>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-medium text-gray-800">{comment.userName}</span>
                    {comment.isAuthor && (
                      <span className="px-1.5 py-0.5 text-[10px] bg-emerald-50 text-emerald-600 rounded">
                        作者
                      </span>
                    )}
                  </div>
                  <p className="text-xs text-gray-600 mt-1">{comment.content}</p>
                  <div className="flex items-center gap-4 mt-2">
                    <span className="text-xs text-gray-400">{formatTime(comment.createTime)}</span>
                    <button
                      onClick={() => handleLikeComment(comment.id, comment.isLiked)}
                      className="flex items-center gap-1 text-xs text-gray-400"
                    >
                      <Heart
                        className={`w-3.5 h-3.5 ${
                          comment.isLiked ? 'fill-red-500 text-red-500' : ''
                        }`}
                      />
                      {comment.likeCount > 0 && comment.likeCount}
                    </button>
                    <button
                      onClick={() => setReplyingTo({
                        commentId: comment.id,
                        userId: comment.userId,
                        userName: comment.userName,
                        isReplyToReply: false,
                      })}
                      className="text-xs text-gray-400"
                    >
                      回复
                    </button>
                    {comment.userId === Number(currentUserId) && (
                      <button
                        onClick={() => {
                          if (comment.hidden === 1) {
                            handleUnhideComment(comment);
                          } else {
                            handleHideComment(comment);
                          }
                        }}
                        className="text-xs text-gray-400"
                      >
                        {comment.hidden === 1 ? '取消隐藏' : '隐藏'}
                      </button>
                    )}
                    {comment.userId === Number(currentUserId) && (
                      <button
                        onClick={() => {
                          setEditingComment(comment);
                          setEditContent(comment.content);
                        }}
                        className="text-xs text-gray-400"
                      >
                        编辑
                      </button>
                    )}
                    {comment.userId === Number(currentUserId) && (
                      <button
                        onClick={() => setDeletingComment(comment)}
                        className="text-xs text-red-400"
                      >
                        删除
                      </button>
                    )}
                  </div>

                  {comment.replies && comment.replies.length > 0 && (
                    <div className="mt-2 pl-2.5 border-l-2 border-gray-100 space-y-2">
                      {comment.replies.map((reply) => (
                        <div key={reply.id} className="flex gap-1.5">
                          <UserCard userId={reply.userId}>
                            <Avatar className="w-5 h-5 flex-shrink-0">
                              <AvatarImage src={reply.userAvatar} />
                              <AvatarFallback className="text-[6px]">{reply.userName?.[0]}</AvatarFallback>
                            </Avatar>
                          </UserCard>
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center gap-1">
                              <span className="text-[10px] font-medium text-gray-800">{reply.userName}</span>
                              {reply.targetUserName && (
                                <>
                                  <span className="text-[10px] text-gray-400">回复</span>
                                  <span className="text-[10px] text-gray-500">@{reply.targetUserName}</span>
                                </>
                              )}
                              {reply.isAuthor && (
                                <span className="px-1 py-0.5 text-[10px] bg-emerald-50 text-emerald-600 rounded">
                                  作者
                                </span>
                              )}
                            </div>
                            <p className="text-[10px] text-gray-600 mt-0.5">{reply.content}</p>
                            <div className="flex items-center gap-4 mt-1">
                              <span className="text-[10px] text-gray-400">{formatTime(reply.createTime)}</span>
                              <button
                                onClick={() => handleLikeComment(reply.id, reply.isLiked)}
                                className="flex items-center gap-1 text-[10px] text-gray-400"
                              >
                                <Heart
                                  className={`w-3 h-3 ${
                                    reply.isLiked ? 'fill-red-500 text-red-500' : ''
                                  }`}
                                />
                                {reply.likeCount > 0 && reply.likeCount}
                              </button>
                              <button
                                onClick={() => setReplyingTo({
                                  commentId: comment.id,
                                  userId: reply.userId,
                                  userName: reply.userName,
                                  replyId: reply.id,
                                  isReplyToReply: true,
                                })}
                                className="text-[10px] text-gray-400 hover:text-emerald-600"
                              >
                                回复
                              </button>
                              {reply.userId === Number(currentUserId) && (
                                <button
                                  onClick={() => {
                                    if (reply.hidden === 1) {
                                      handleUnhideReply(reply);
                                    } else {
                                      handleHideReply(reply);
                                    }
                                  }}
                                  className="text-[10px] text-gray-400"
                                >
                                  {reply.hidden === 1 ? '取消隐藏' : '隐藏'}
                                </button>
                              )}
                              {reply.userId === Number(currentUserId) && (
                                <button
                                  onClick={() => {
                                    setEditingReply(reply);
                                    setEditContent(reply.content);
                                  }}
                                  className="text-[10px] text-gray-400"
                                >
                                  编辑
                                </button>
                              )}
                              {reply.userId === Number(currentUserId) && (
                                <button
                                  onClick={() => setDeletingReply(reply)}
                                  className="text-[10px] text-red-400"
                                >
                                  删除
                                </button>
                              )}
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>

        {hasMoreComments && comments.length > 0 && (
          <div className="py-4 text-center">
            <button
              onClick={handleLoadMoreComments}
              disabled={commentsLoading}
              className="text-sm text-emerald-600"
            >
              {commentsLoading ? '加载中...' : '加载更多评论'}
            </button>
          </div>
        )}
      </div>

      <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-100 p-2 z-[100]">
        <div className="max-w-md mx-auto">
          {replyingTo && (
            <div className="flex items-center justify-between mb-1.5 px-1">
              <span className="text-xs text-gray-500">
                {replyingTo.isReplyToReply
                  ? `回复 ${replyingTo.userName} 的回复`
                  : `回复 ${replyingTo.userName}`}
              </span>
              <button
                onClick={() => setReplyingTo(null)}
                className="text-xs text-gray-400 hover:text-gray-600"
              >
                取消回复
              </button>
            </div>
          )}
          <div className="flex items-center gap-2">
            <input
              type="text"
              value={commentText}
              onChange={(e) => setCommentText(e.target.value)}
              placeholder={replyingTo ? '写下你的回复...' : '说点什么...'}
              className="flex-1 px-3 py-1.5 bg-gray-100 text-sm outline-none focus:ring-2 focus:ring-emerald-600/20"
            />
            <button
              onClick={handleSubmitComment}
              disabled={submitting || !commentText.trim()}
              className="w-8 h-8 flex items-center justify-center bg-emerald-600 text-white disabled:opacity-50"
            >
              {submitting ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <Send className="w-4 h-4" />
              )}
            </button>
          </div>
        </div>
      </div>

      <Dialog open={previewOpen} onOpenChange={setPreviewOpen}>
        <DialogContent 
          className="max-w-full max-h-full w-full h-full bg-black/90 border-none p-0 flex items-center justify-center"
          showCloseButton={false}
        >
          <DialogDescription className="sr-only">预览心得图片</DialogDescription>
          <button
            onClick={() => setPreviewOpen(false)}
            className="absolute top-4 right-4 z-50 p-2 text-white/80 hover:text-white"
          >
            <X className="w-6 h-6" />
          </button>
          
          {post.images && post.images.length > 1 && (
            <>
              <button
                onClick={() => setPreviewIndex((prev) => (prev > 0 ? prev - 1 : post.images!.length - 1))}
                className="absolute left-4 z-50 p-2 text-white/80 hover:text-white"
              >
                <ChevronLeft className="w-8 h-8" />
              </button>
              <button
                onClick={() => setPreviewIndex((prev) => (prev < post.images!.length - 1 ? prev + 1 : 0))}
                className="absolute right-4 z-50 p-2 text-white/80 hover:text-white"
              >
                <ChevronRight className="w-8 h-8" />
              </button>
            </>
          )}

          {post.images && (
            <img
              src={post.images[previewIndex]}
              alt=""
              className="max-w-full max-h-full object-contain"
            />
          )}

          {post.images && post.images.length > 1 && (
            <div className="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-1.5">
              {post.images.map((_, index) => (
                <button
                  key={index}
                  onClick={() => setPreviewIndex(index)}
                  className={`w-2 h-2 rounded-full transition-colors ${
                    index === previewIndex ? 'bg-white' : 'bg-white/40'
                  }`}
                />
              ))}
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* 删除确认弹窗 */}
      <Dialog open={showDeleteConfirm} onOpenChange={setShowDeleteConfirm}>
        <DialogContent className="max-w-[16rem]">
          <DialogDescription className="sr-only">确认是否删除当前心得</DialogDescription>
          <div className="text-center py-3">
            <Trash2 className="w-10 h-10 text-red-500 mx-auto mb-2" />
            <h3 className="text-sm font-medium mb-1.5">确认删除</h3>
            <p className="text-xs text-gray-500 mb-3">删除后将无法恢复，确定要删除这条心得吗？</p>
            <div className="flex gap-2">
              <button
                onClick={() => setShowDeleteConfirm(false)}
                className="flex-1 py-1.5 text-xs text-gray-600 bg-gray-100 rounded-lg"
              >
                取消
              </button>
              <button
                onClick={handleDelete}
                disabled={deleting}
                className="flex-1 py-1.5 text-xs text-white bg-red-500 rounded-lg disabled:opacity-50"
              >
                {deleting ? '删除中...' : '确认删除'}
              </button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* 隐藏确认弹窗 */}
      <Dialog open={showHideConfirm} onOpenChange={setShowHideConfirm}>
        <DialogContent className="max-w-[16rem]">
          <DialogDescription className="sr-only">确认是否隐藏或取消隐藏当前心得</DialogDescription>
          <div className="text-center py-3">
            {isHidden ? (
              <>
                <Eye className="w-10 h-10 text-emerald-600 mx-auto mb-2" />
                <h3 className="text-sm font-medium mb-1.5">取消隐藏</h3>
                <p className="text-xs text-gray-500 mb-3">取消隐藏后，其他用户将可以看到这条心得</p>
              </>
            ) : (
              <>
                <EyeOff className="w-10 h-10 text-gray-400 mx-auto mb-2" />
                <h3 className="text-sm font-medium mb-1.5">隐藏心得</h3>
                <p className="text-xs text-gray-500 mb-3">隐藏后其他用户将无法看到这条心得，你可以随时取消隐藏</p>
              </>
            )}
            <div className="flex gap-2">
              <button
                onClick={() => setShowHideConfirm(false)}
                className="flex-1 py-1.5 text-xs text-gray-600 bg-gray-100 rounded-lg"
              >
                取消
              </button>
              <button
                onClick={handleHide}
                disabled={hiding}
                className="flex-1 py-1.5 text-xs text-white bg-emerald-600 rounded-lg disabled:opacity-50"
              >
                {hiding ? '处理中...' : isHidden ? '取消隐藏' : '确认隐藏'}
              </button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* 编辑评论弹窗 */}
      <Dialog open={!!editingComment} onOpenChange={() => setEditingComment(null)}>
        <DialogContent className="max-w-[18rem]">
          <DialogDescription className="sr-only">编辑评论内容</DialogDescription>
          <div className="py-3">
            <h3 className="text-sm font-medium mb-2">编辑评论</h3>
            <textarea
              value={editContent}
              onChange={(e) => setEditContent(e.target.value)}
              placeholder="请输入评论内容"
              className="w-full h-20 px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-emerald-600/20"
            />
            <div className="flex gap-2 mt-3">
              <button
                onClick={() => {
                  setEditingComment(null);
                  setEditContent('');
                }}
                className="flex-1 py-1.5 text-xs text-gray-600 bg-gray-100 rounded-lg"
              >
                取消
              </button>
              <button
                onClick={handleEditComment}
                disabled={!editContent.trim()}
                className="flex-1 py-1.5 text-xs text-white bg-emerald-600 rounded-lg disabled:opacity-50"
              >
                保存
              </button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* 删除评论确认弹窗 */}
      <Dialog open={!!deletingComment} onOpenChange={() => setDeletingComment(null)}>
        <DialogContent className="max-w-[16rem]">
          <DialogDescription className="sr-only">确认删除评论及其回复</DialogDescription>
          <div className="text-center py-3">
            <Trash2 className="w-10 h-10 text-red-500 mx-auto mb-2" />
            <h3 className="text-sm font-medium mb-1.5">确认删除</h3>
            <p className="text-xs text-gray-500 mb-3">删除后将同时删除该评论下的所有回复，确定要删除吗？</p>
            <div className="flex gap-2">
              <button
                onClick={() => setDeletingComment(null)}
                className="flex-1 py-1.5 text-xs text-gray-600 bg-gray-100 rounded-lg"
              >
                取消
              </button>
              <button
                onClick={handleDeleteComment}
                className="flex-1 py-1.5 text-xs text-white bg-red-500 rounded-lg"
              >
                确认删除
              </button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* 编辑回复弹窗 */}
      <Dialog open={!!editingReply} onOpenChange={() => setEditingReply(null)}>
        <DialogContent className="max-w-[18rem]">
          <DialogDescription className="sr-only">编辑回复内容</DialogDescription>
          <div className="py-3">
            <h3 className="text-sm font-medium mb-2">编辑回复</h3>
            <textarea
              value={editContent}
              onChange={(e) => setEditContent(e.target.value)}
              placeholder="请输入回复内容"
              className="w-full h-20 px-2.5 py-1.5 text-xs border border-gray-200 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-emerald-600/20"
            />
            <div className="flex gap-2 mt-3">
              <button
                onClick={() => {
                  setEditingReply(null);
                  setEditContent('');
                }}
                className="flex-1 py-1.5 text-xs text-gray-600 bg-gray-100 rounded-lg"
              >
                取消
              </button>
              <button
                onClick={handleEditReply}
                disabled={!editContent.trim()}
                className="flex-1 py-1.5 text-xs text-white bg-emerald-600 rounded-lg disabled:opacity-50"
              >
                保存
              </button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* 删除回复确认弹窗 */}
      <Dialog open={!!deletingReply} onOpenChange={() => setDeletingReply(null)}>
        <DialogContent className="max-w-[16rem]">
          <DialogDescription className="sr-only">确认删除当前回复</DialogDescription>
          <div className="text-center py-3">
            <Trash2 className="w-10 h-10 text-red-500 mx-auto mb-2" />
            <h3 className="text-sm font-medium mb-1.5">确认删除</h3>
            <p className="text-xs text-gray-500 mb-3">删除后将无法恢复，确定要删除这条回复吗？</p>
            <div className="flex gap-2">
              <button
                onClick={() => setDeletingReply(null)}
                className="flex-1 py-1.5 text-xs text-gray-600 bg-gray-100 rounded-lg"
              >
                取消
              </button>
              <button
                onClick={handleDeleteReply}
                className="flex-1 py-1.5 text-xs text-white bg-red-500 rounded-lg"
              >
                确认删除
              </button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}

