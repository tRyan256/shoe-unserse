import { useState, useEffect, useCallback, useRef } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Search, Camera, Heart, Grid, List, ChevronDown, Loader2, X } from 'lucide-react';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { experienceApi } from '@/api';
import { useUserStore } from '@/stores';
import { toast } from 'sonner';
import type { ExperiencePost, PageResult } from '@/types';

type FilterType = 'all' | 'following' | 'mine' | 'liked' | 'product';
type SortType = 'time' | 'time_asc' | 'like';
type ViewMode = 'grid' | 'list';

const filterOptions: { value: FilterType; label: string; requireLogin?: boolean }[] = [
  { value: 'all', label: '全部' },
  { value: 'following', label: '关注', requireLogin: true },
  { value: 'mine', label: '我的', requireLogin: true },
  { value: 'liked', label: '点赞过', requireLogin: true },
];

const sortOptions: { value: SortType; label: string }[] = [
  { value: 'time', label: '最新发布' },
  { value: 'time_asc', label: '最早发布' },
  { value: 'like', label: '点赞最多' },
];

export default function CommunityPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const isLoggedIn = useUserStore((state) => state.isLoggedIn);
  const currentUserId = useUserStore((state) => state.user?.id);
  const token = useUserStore((state) => state.token);
  const user = useUserStore((state) => state.user);

  const SCROLL_KEY = 'community_scroll_position';
  const POSTS_KEY = 'community_posts_cache';
  const STATE_KEY = 'community_state_cache';

  const [activeFilter, setActiveFilter] = useState<FilterType>('all');
  const [activeSort, setActiveSort] = useState<SortType>('time');
  const [viewMode, setViewMode] = useState<ViewMode>('grid');
  const [showSortDropdown, setShowSortDropdown] = useState(false);

  const [posts, setPosts] = useState<ExperiencePost[]>([]);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);

  const [likedPosts, setLikedPosts] = useState<Set<number>>(new Set());
  const [searchInput, setSearchInput] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const skipNextLoadRef = useRef(false);

  // 从详情页返回时恢复滚动位置和数据
  useEffect(() => {
    if (location.state?.fromDetail) {
      const cachedState = sessionStorage.getItem(STATE_KEY);
      if (cachedState) {
        try {
          const state = JSON.parse(cachedState);
          setActiveFilter(state.activeFilter || 'all');
          setActiveSort(state.activeSort || 'time');
          setViewMode(state.viewMode || 'grid');
          setSearchInput(state.searchInput || '');
          setSearchKeyword(state.searchKeyword || '');
        } catch {
          console.error('Failed to restore state');
        }
      }
      
      const cachedPosts = sessionStorage.getItem(POSTS_KEY);
      if (cachedPosts) {
        try {
          const data = JSON.parse(cachedPosts);
          setPosts(data.posts || []);
          setTotal(data.total || 0);
          setPage(data.page || 1);
          setHasMore(data.hasMore !== false);
          // 标记跳过下一次数据加载
          skipNextLoadRef.current = true;
        } catch {
          console.error('Failed to restore posts');
        }
      }
    }
  }, [location.state?.fromDetail]);

  // 恢复滚动位置 - 在 posts 数据恢复后执行
  useEffect(() => {
    if (location.state?.fromDetail && posts.length > 0) {
      const savedPosition = sessionStorage.getItem(SCROLL_KEY);
      if (savedPosition) {
        requestAnimationFrame(() => {
          setTimeout(() => {
            window.scrollTo(0, parseInt(savedPosition, 10));
          }, 50);
        });
      }
    }
  }, [location.state?.fromDetail, posts]);

  // 初始加载或筛选条件变化时加载数据
  useEffect(() => {
    const controller = new AbortController();
    
    // 如果是从详情页返回且恢复了缓存数据，跳过本次加载
    if (skipNextLoadRef.current) {
      skipNextLoadRef.current = false;
      return;
    }
    
    const loadData = async () => {
      const loggedIn = !!token && !!user;
      if (activeFilter !== 'all' && !loggedIn) {
        setLoading(false);
        return;
      }
      
      setLoading(true);
      try {
        let result: PageResult<ExperiencePost> | null = null;

        switch (activeFilter) {
          case 'following':
            result = await experienceApi.listFollowingPosts({ sortBy: activeSort, page: 1, size: 20 });
            break;
          case 'mine':
            if (currentUserId) {
              result = await experienceApi.listUserPosts(Number(currentUserId), { page: 1, size: 20 });
            }
            break;
          case 'liked':
            result = await experienceApi.listLikedPosts({ sortBy: activeSort, page: 1, size: 20 });
            break;
          case 'all':
          default:
            result = await experienceApi.listAllPosts({ sortBy: activeSort, keyword: searchKeyword || undefined, page: 1, size: 20 });
            break;
        }

        if (result && !controller.signal.aborted) {
          // 调试：打印每个帖子的图片数量
          result.records.forEach((post: ExperiencePost) => {
            console.log(`Post ${post.id} (${post.userName}): images count = ${post.images?.length || 0}, images =`, post.images);
          });
          setPosts(result.records);
          setTotal(result.total || result.records.length);
          setHasMore(result.records.length === 20);
        }
      } catch (err) {
        if (!controller.signal.aborted) {
          console.error('Failed to fetch posts:', err);
          toast.error('加载失败，请重试');
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    };
    
    setPage(1);
    setPosts([]);
    setHasMore(true);
    loadData();
    
    return () => controller.abort();
  }, [activeFilter, activeSort, searchKeyword, token, user, currentUserId]);

  // 保存滚动位置
  useEffect(() => {
    const handleScroll = () => {
      sessionStorage.setItem(SCROLL_KEY, String(window.scrollY));
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  // 点击心得卡片时保存当前状态
  const handlePostClick = (postId: number) => {
    sessionStorage.setItem(POSTS_KEY, JSON.stringify({
      posts,
      total,
      page,
      hasMore,
    }));
    
    sessionStorage.setItem(STATE_KEY, JSON.stringify({
      activeFilter,
      activeSort,
      viewMode,
      searchInput,
      searchKeyword,
    }));
  };

  const fetchPosts = useCallback(async (pageNum: number, refresh = false) => {
    const loggedIn = !!token && !!user;
    if (activeFilter !== 'all' && !loggedIn) {
      toast.info('请先登录');
      return;
    }

    setLoading(true);
    try {
      let result: PageResult<ExperiencePost> | null = null;

      switch (activeFilter) {
        case 'following':
          result = await experienceApi.listFollowingPosts({ sortBy: activeSort, page: pageNum, size: 20 });
          break;
        case 'mine':
          if (currentUserId) {
            result = await experienceApi.listUserPosts(Number(currentUserId), { page: pageNum, size: 20 });
          }
          break;
        case 'liked':
          result = await experienceApi.listLikedPosts({ sortBy: activeSort, page: pageNum, size: 20 });
          break;
        case 'all':
        default:
          result = await experienceApi.listAllPosts({ sortBy: activeSort, keyword: searchKeyword || undefined, page: pageNum, size: 20 });
          break;
      }

      if (result) {
        if (refresh) {
          setPosts(result.records);
        } else {
          setPosts((prev) => [...prev, ...result!.records]);
        }
        setTotal(result.total || result.records.length);
        setHasMore(result.records.length === 20);
      }
    } catch (err) {
      console.error('Failed to fetch posts:', err);
      toast.error('加载失败，请重试');
    } finally {
      setLoading(false);
    }
  }, [activeFilter, activeSort, token, user, currentUserId, searchKeyword]);

  useEffect(() => {
    if (page > 1) {
      fetchPosts(page);
    }
  }, [page]);

  const handleLoadMore = () => {
    if (!loading && hasMore) {
      setPage((prev) => prev + 1);
    }
  };

  const handleLike = async (postId: number, e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();

    if (!isLoggedIn()) {
      toast.info('请先登录');
      return;
    }

    const isLiked = likedPosts.has(postId) || posts.find(p => p.id === postId)?.isLiked;

    try {
      if (isLiked) {
        await experienceApi.unlikePost(postId);
        setLikedPosts((prev) => {
          const next = new Set(prev);
          next.delete(postId);
          return next;
        });
        setPosts((prev) =>
          prev.map((p) =>
            p.id === postId
              ? { ...p, isLiked: false, likeCount: Math.max(0, p.likeCount - 1) }
              : p
          )
        );
      } else {
        await experienceApi.likePost(postId);
        setLikedPosts((prev) => new Set(prev).add(postId));
        setPosts((prev) =>
          prev.map((p) =>
            p.id === postId
              ? { ...p, isLiked: true, likeCount: p.likeCount + 1 }
              : p
          )
        );
      }
    } catch {
      toast.error('操作失败，请重试');
    }
  };

  const handleFollowInList = async (userId: number, isFollowed: boolean, e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();

    if (!isLoggedIn()) {
      toast.info('请先登录');
      return;
    }

    try {
      if (isFollowed) {
        await experienceApi.unfollowUser(userId);
        setPosts((prev) =>
          prev.map((p) =>
            p.userId === userId ? { ...p, isFollowed: false } : p
          )
        );
        toast.success('已取消关注');
      } else {
        await experienceApi.followUser(userId);
        setPosts((prev) =>
          prev.map((p) =>
            p.userId === userId ? { ...p, isFollowed: true } : p
          )
        );
        toast.success('关注成功');
      }
    } catch {
      toast.error('操作失败');
    }
  };

  const handleFilterChange = (filter: FilterType) => {
    if (filter !== 'all' && !isLoggedIn()) {
      toast.info('请先登录');
      return;
    }
    setActiveFilter(filter);
  };

  const handlePublishClick = () => {
    if (!isLoggedIn()) {
      toast.info('请先登录');
      return;
    }
    navigate('/community/publish');
  };

  const handleSearch = () => {
    if (activeFilter !== 'all') {
      setActiveFilter('all');
    }
    setSearchKeyword(searchInput.trim());
    setPage(1);
    setPosts([]);
    setHasMore(true);
  };

  const handleClearSearch = () => {
    setSearchInput('');
    setSearchKeyword('');
    setPage(1);
    setPosts([]);
    setHasMore(true);
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

  return (
    <div className="min-h-screen bg-gray-100 pb-20">
      <header className="sticky top-0 z-40 bg-white shadow-sm">
        <div className="flex items-center gap-3 px-4 h-12">
          <div className="flex items-center gap-2 flex-1 bg-gray-100 px-4 py-1.5 rounded-full">
            <input
              type="text"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              placeholder="搜索感兴趣的内容"
              className="flex-1 bg-transparent text-sm outline-none placeholder:text-gray-400"
            />
            {searchInput && (
              <button
                onClick={handleClearSearch}
                className="text-gray-400 hover:text-gray-600"
              >
                <X className="w-4 h-4" />
              </button>
            )}
            <button
              onClick={handleSearch}
              className="text-gray-400 hover:text-emerald-600 transition-colors"
            >
              <Search className="w-4 h-4" />
            </button>
          </div>
          <button
            onClick={handlePublishClick}
            className="w-10 h-10 flex items-center justify-center text-emerald-700 hover:text-emerald-600 transition-colors"
          >
            <Camera className="w-6 h-6" strokeWidth={1.5} />
          </button>
        </div>

        <div className="flex items-center gap-0.5 px-4 pb-0.5">
          <div className="flex flex-1 overflow-x-auto scrollbar-hide">
            {filterOptions.map((option) => (
              <button
                key={option.value}
                onClick={() => handleFilterChange(option.value)}
                className={`px-1.5 py-0.5 text-xs whitespace-nowrap transition-all duration-300 rounded-full ${
                  activeFilter === option.value
                    ? 'text-emerald-600 font-medium'
                    : 'text-gray-500 hover:bg-gray-100'
                }`}
              >
                {option.label}
              </button>
            ))}
          </div>

          <div className="flex items-center gap-2">
            <div className="relative">
              <button
                onClick={() => setShowSortDropdown(!showSortDropdown)}
                className="flex items-center gap-1 px-2 py-1 text-xs text-gray-500 hover:bg-gray-100 rounded-full"
              >
                {sortOptions.find((s) => s.value === activeSort)?.label}
                <ChevronDown className={`w-3 h-3 transition-transform ${showSortDropdown ? 'rotate-180' : ''}`} />
              </button>

              <AnimatePresence>
                {showSortDropdown && (
                  <motion.div
                    initial={{ opacity: 0, y: -10 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -10 }}
                    className="absolute right-0 top-full mt-1 bg-white rounded-lg shadow-lg border border-gray-100 py-1 min-w-[120px] z-50"
                  >
                    {sortOptions.map((option) => (
                      <button
                        key={option.value}
                        onClick={() => {
                          setActiveSort(option.value);
                          setShowSortDropdown(false);
                        }}
                        className={`w-full px-4 py-2 text-sm text-left hover:bg-gray-50 ${
                          activeSort === option.value ? 'text-emerald-600 font-medium' : 'text-gray-600'
                        }`}
                      >
                        {option.label}
                      </button>
                    ))}
                  </motion.div>
                )}
              </AnimatePresence>
            </div>

            <div className="flex bg-gray-100 rounded-full p-0.5">
              <button
                onClick={() => setViewMode('grid')}
                className={`p-1 rounded-full transition-colors ${
                  viewMode === 'grid' ? 'bg-white shadow-sm' : 'text-gray-400'
                }`}
              >
                <Grid className="w-3.5 h-3.5" />
              </button>
              <button
                onClick={() => setViewMode('list')}
                className={`p-1 rounded-full transition-colors ${
                  viewMode === 'list' ? 'bg-white shadow-sm' : 'text-gray-400'
                }`}
              >
                <List className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        </div>
      </header>

      <div className="p-3">
        {posts.length === 0 && !loading && (
          <div className="flex flex-col items-center justify-center py-20 text-gray-400">
            <Camera className="w-16 h-16 mb-4 opacity-50" />
            <p className="text-sm">暂无心得内容</p>
            {activeFilter !== 'all' && (
              <p className="text-xs mt-1">切换筛选条件看看其他内容</p>
            )}
          </div>
        )}

        {viewMode === 'grid' ? (
          <div className="columns-2 gap-x-2 px-2">
            {posts.map((post, index) => (
              <motion.div
                key={post.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.02 }}
                className="break-inside-avoid mb-1"
              >
                <Link
                  to={`/community/detail/${post.id}`}
                  onClick={() => handlePostClick(post.id)}
                >
                  <div className="bg-white rounded-md overflow-hidden shadow-sm hover:shadow-md transition-shadow duration-300">
                    <div className="relative">
                      <img
                        src={post.images?.[0] || '/placeholder.png'}
                        alt={post.content}
                        className="w-full object-cover"
                        loading="lazy"
                      />
                      <div className="absolute inset-0 bg-gradient-to-t from-black/30 via-transparent to-transparent" />
                    </div>

                    <div className="p-1.5">
                      <p className="text-[11px] line-clamp-2 mb-1 text-gray-800">{post.content}</p>

                      {post.productName && (
                        <div className="flex items-center gap-1 bg-gray-100 rounded p-1 mb-1">
                          <img
                            src={post.productImage || '/placeholder.png'}
                            alt={post.productName}
                            className="w-4 h-4 rounded object-cover"
                          />
                          <span className="text-[8px] text-gray-600 line-clamp-1 flex-1">
                            {post.productName}
                          </span>
                        </div>
                      )}

                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-1">
                          <Avatar className="w-3.5 h-3.5 border border-gray-100">
                            <AvatarImage src={post.userAvatar} />
                            <AvatarFallback className="text-[6px]">{post.userName?.[0]}</AvatarFallback>
                          </Avatar>
                          <span className="text-[8px] text-gray-500 line-clamp-1 max-w-[40px]">
                            {post.userName}
                          </span>
                        </div>

                        <button
                          onClick={(e) => handleLike(post.id, e)}
                          className="flex items-center gap-0.5"
                        >
                          <Heart
                            className={`w-3 h-3 transition-all duration-300 ${
                              likedPosts.has(post.id) || post.isLiked
                                ? 'fill-red-500 text-red-500 scale-110'
                                : 'text-gray-400'
                            }`}
                          />
                          <span className="text-[8px] text-gray-500">
                            {post.likeCount + (likedPosts.has(post.id) && !post.isLiked ? 1 : 0)}
                          </span>
                        </button>
                      </div>
                    </div>
                  </div>
                </Link>
              </motion.div>
            ))}
          </div>
        ) : (
          <div className="space-y-3">
            {posts.map((post, index) => (
              <motion.div
                key={post.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.02 }}
              >
                <Link 
                  to={`/community/detail/${post.id}`}
                  onClick={() => handlePostClick(post.id)}
                >
                  <div className="bg-white rounded-xl overflow-hidden shadow-sm">
                    <div className="p-2">
                      <div className="flex items-center gap-2 mb-1">
                        <Avatar className="w-6 h-6">
                          <AvatarImage src={post.userAvatar} />
                          <AvatarFallback>{post.userName?.[0]}</AvatarFallback>
                        </Avatar>
                        <div className="flex-1">
                          <p className="text-xs font-medium text-gray-800">{post.userName}</p>
                          <p className="text-xs text-gray-400">{formatTime(post.createTime)}</p>
                        </div>
                        {post.isFollowed === false && post.userId !== Number(currentUserId) && (
                          <button
                            onClick={(e) => handleFollowInList(post.userId, false, e)}
                            className="px-2 py-0.5 text-xs text-emerald-600 border border-emerald-600 rounded-full"
                          >
                            关注
                          </button>
                        )}
                        {post.isFollowed === true && post.userId !== Number(currentUserId) && (
                          <button
                            onClick={(e) => handleFollowInList(post.userId, true, e)}
                            className="px-2 py-0.5 text-xs text-gray-500 bg-gray-100 rounded-full"
                          >
                            已关注
                          </button>
                        )}
                      </div>

                      <p className="text-xs text-gray-800 mb-1 leading-relaxed line-clamp-2">{post.content}</p>

                      {post.images && post.images.length > 0 && (
                        <div className={`grid gap-1 mb-1 ${
                          post.images.length === 1 ? 'grid-cols-1 w-1/2' :
                          post.images.length === 2 ? 'grid-cols-2' :
                          'grid-cols-3'
                        }`}>
                          {post.images.slice(0, post.images.length > 3 ? 3 : post.images.length).map((img, imgIndex, arr) => {
                            const showMore = post.images!.length > 3 && imgIndex === 2;
                            const remainingCount = post.images!.length - 3;
                            return (
                              <div key={imgIndex} className="relative">
                                <img
                                  src={img}
                                  alt=""
                                  className="w-full aspect-square object-cover rounded"
                                  loading="lazy"
                                />
                                {showMore && (
                                  <div className="absolute inset-0 bg-black/50 rounded flex items-center justify-center">
                                    <span className="text-white text-sm font-medium">还有{remainingCount}张</span>
                                  </div>
                                )}
                              </div>
                            );
                          })}
                        </div>
                      )}

                      {post.productName && (
                        <div className="flex items-center gap-2 bg-gray-50 rounded p-1 mb-1">
                          <img
                            src={post.productImage || '/placeholder.png'}
                            alt={post.productName}
                            className="w-6 h-6 rounded object-cover"
                          />
                          <span className="text-xs text-gray-600 flex-1">{post.productName}</span>
                        </div>
                      )}

                      <div className="flex items-center gap-4 text-gray-400">
                        <button
                          onClick={(e) => handleLike(post.id, e)}
                          className="flex items-center gap-1"
                        >
                          <Heart
                            className={`w-4 h-4 transition-all ${
                              likedPosts.has(post.id) || post.isLiked
                                ? 'fill-red-500 text-red-500'
                                : ''
                            }`}
                          />
                          <span className="text-xs">{post.likeCount + (likedPosts.has(post.id) && !post.isLiked ? 1 : 0)}</span>
                        </button>
                        <span className="text-xs">{post.commentCount} 评论</span>
                      </div>
                    </div>
                  </div>
                </Link>
              </motion.div>
            ))}
          </div>
        )}

        {loading && (
          <div className="flex justify-center py-8">
            <Loader2 className="w-6 h-6 text-emerald-600 animate-spin" />
          </div>
        )}

        {hasMore && !loading && posts.length > 0 && (
          <div className="flex justify-center py-4">
            <button
              onClick={handleLoadMore}
              className="text-sm text-gray-400 hover:text-emerald-600"
            >
              加载更多
            </button>
          </div>
        )}

        {!hasMore && posts.length > 0 && (
          <div className="text-center py-4 text-sm text-gray-400">
            没有更多了
          </div>
        )}
      </div>

    </div>
  );
}

