import { MapPin, Phone, User, Edit2, Trash2 } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog';
import type { AddressBook } from '@/api/user/addressApi';

interface AddressCardProps {
  address: AddressBook;
  onEdit: (address: AddressBook) => void;
  onDelete: (id: number) => void;
  onSetDefault: (id: number) => void;
}

export default function AddressCard({ address, onEdit, onDelete, onSetDefault }: AddressCardProps) {
  const fullAddress = `${address.provinceName}${address.cityName}${address.districtName}${address.detail}`;
  const sexLabel = address.sex === '1' ? '先生' : address.sex === '2' ? '女士' : '';

  return (
    <div className="bg-white rounded-xl p-4 relative">
      {/* 默认标签 */}
      {address.isDefault === 1 && (
        <Badge className="absolute top-4 right-4 bg-emerald-700">默认</Badge>
      )}

      {/* 收货人信息 */}
      <div className="mb-3">
        <div className="flex items-center gap-2 mb-2">
          <User className="w-4 h-4 text-gray-400" />
          <span className="font-medium">
            {address.consignee} {sexLabel}
          </span>
        </div>
        <div className="flex items-center gap-2 mb-2">
          <Phone className="w-4 h-4 text-gray-400" />
          <span className="text-sm text-gray-600">{address.phone}</span>
        </div>
        <div className="flex items-start gap-2">
          <MapPin className="w-4 h-4 text-gray-400 mt-0.5 flex-shrink-0" />
          <span className="text-sm text-gray-600">{fullAddress}</span>
        </div>
      </div>

      {/* 标签 */}
      {address.label && (
        <div className="mb-3">
          <Badge variant="outline" className="text-xs">
            {address.label}
          </Badge>
        </div>
      )}

      {/* 操作按钮 */}
      <div className="flex items-center gap-2 pt-3 border-t border-gray-100">
        {address.isDefault !== 1 && (
          <Button
            variant="ghost"
            size="sm"
            onClick={() => onSetDefault(address.id)}
            className="text-gray-600 hover:text-gray-900"
          >
            设为默认
          </Button>
        )}
        <Button
          variant="ghost"
          size="sm"
          onClick={() => onEdit(address)}
          className="text-emerald-700 hover:text-emerald-800"
        >
          <Edit2 className="w-4 h-4 mr-1" />
          编辑
        </Button>
        <AlertDialog>
          <AlertDialogTrigger asChild>
            <Button
              variant="ghost"
              size="sm"
              className="text-red-600 hover:text-red-700"
            >
              <Trash2 className="w-4 h-4 mr-1" />
              删除
            </Button>
          </AlertDialogTrigger>
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>确认删除</AlertDialogTitle>
              <AlertDialogDescription>
                确定要删除这个收货地址吗？此操作无法撤销。
              </AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel>取消</AlertDialogCancel>
              <AlertDialogAction
                onClick={() => onDelete(address.id)}
                className="bg-red-600 hover:bg-red-700"
              >
                删除
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </div>
    </div>
  );
}


