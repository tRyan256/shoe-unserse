import { useState, useEffect } from 'react';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { experienceApi } from '@/api';
import { useUserStore } from '@/stores';
import { toast } from 'sonner';
import { Loader2 } from 'lucide-react';
import type { ExperienceUserProfile } from '@/types';

interface UserCardProps {
  userId: number;
  children: React.ReactNode;
}

export function UserCard({ userId, children }: UserCardProps) {
  const [open, setOpen] = useState(false);
  const [profile, setProfile] = useState<ExperienceUserProfile | null>(null);
  const [loading, setLoading] = useState(false);
  const isLoggedIn = useUserStore((state) => state.isLoggedIn);
  const currentUserId = useUserStore((state) => state.user?.id);

  useEffect(() => {
    if (open && userId) {
      fetchProfile();
    }
  }, [open, userId]);

  const fetchProfile = async () => {
    setLoading(true);
    try {
      const data = await experienceApi.getUserProfile(userId);
      setProfile(data);
    } catch {
      console.error('Failed to fetch user profile');
    } finally {
      setLoading(false);
    }
  };

  const handleFollow = async () => {
    if (!isLoggedIn()) {
      toast.info('请先登录');
      return;
    }

    if (!profile) return;

    try {
      if (profile.isFollowed) {
        await experienceApi.unfollowUser(userId);
        setProfile({ ...profile, isFollowed: false, followerCount: profile.followerCount - 1 });
        toast.success('已取消关注');
      } else {
        await experienceApi.followUser(userId);
        setProfile({ ...profile, isFollowed: true, followerCount: profile.followerCount + 1 });
        toast.success('关注成功');
      }
    } catch {
      toast.error('操作失败');
    }
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <div className="cursor-pointer">{children}</div>
      </PopoverTrigger>
      <PopoverContent
        className="w-52 p-0 rounded-lg overflow-hidden"
        align="start"
        side="bottom"
        sideOffset={8}
      >
        {loading ? (
          <div className="flex items-center justify-center py-6">
            <Loader2 className="w-5 h-5 text-emerald-600 animate-spin" />
          </div>
        ) : profile ? (
          <div className="p-3">
            <div className="flex items-center gap-2 mb-2">
              <Avatar className="w-9 h-9 border border-emerald-100">
                <AvatarImage src={profile.userAvatar} />
                <AvatarFallback className="text-sm">{profile.userName?.[0]}</AvatarFallback>
              </Avatar>
              <div className="flex-1 min-w-0">
                <p className="text-xs font-medium text-gray-800 truncate">{profile.userName}</p>
                <p className="text-[10px] text-gray-400">{profile.postCount} 篇心得</p>
              </div>
            </div>

            <div className="grid grid-cols-3 gap-1.5 mb-2 text-center">
              <div className="bg-gray-50 rounded py-1.5">
                <p className="text-xs font-semibold text-gray-800">{profile.likedCount}</p>
                <p className="text-[8px] text-gray-400">获赞</p>
              </div>
              <div className="bg-gray-50 rounded py-1.5">
                <p className="text-xs font-semibold text-gray-800">{profile.followingCount}</p>
                <p className="text-[8px] text-gray-400">关注</p>
              </div>
              <div className="bg-gray-50 rounded py-1.5">
                <p className="text-xs font-semibold text-gray-800">{profile.followerCount}</p>
                <p className="text-[8px] text-gray-400">粉丝</p>
              </div>
            </div>

            {userId !== Number(currentUserId) && (
              <button
                onClick={handleFollow}
                className={`w-full py-1.5 text-xs rounded-full transition-colors ${
                  profile.isFollowed
                    ? 'bg-gray-100 text-gray-500'
                    : 'bg-emerald-600 text-white hover:bg-emerald-700'
                }`}
              >
                {profile.isFollowed ? '已关注' : '关注'}
              </button>
            )}
          </div>
        ) : (
          <div className="py-6 text-center text-gray-400 text-xs">加载失败</div>
        )}
      </PopoverContent>
    </Popover>
  );
}


