import { motion } from 'framer-motion';
import { MapPin, Edit2, Check } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import type { Address } from '@/types';
import { ADDRESS_LABELS } from '@/constants';

interface AddressCardProps {
  address: Address;
  isSelected?: boolean;
  showActions?: boolean;
  onSelect?: (id: number) => void;
  onEdit?: (id: number) => void;
  onSetDefault?: (id: number) => void;
}

export function AddressCard({
  address,
  isSelected = false,
  showActions = true,
  onSelect,
  onEdit,
  onSetDefault,
}: AddressCardProps) {
  const labelConfig = ADDRESS_LABELS.find((l) => l.value === address.label);

  return (
    <motion.div
      whileTap={{ scale: 0.98 }}
      onClick={() => onSelect?.(address.id)}
      className={`relative p-4 bg-white rounded-xl border-2 transition-colors ${
        isSelected ? 'border-black' : 'border-transparent'
      }`}
    >
      {isSelected && (
        <div className="absolute top-2 right-2 w-6 h-6 bg-black rounded-full flex items-center justify-center">
          <Check className="w-4 h-4 text-white" />
        </div>
      )}

      <div className="flex items-start gap-3">
        <div className="mt-1">
          <MapPin className="w-5 h-5 text-gray-400" />
        </div>
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-1">
            <span className="font-medium text-gray-900">{address.consignee}</span>
            <span className="text-sm text-gray-500">{address.phone}</span>
            {address.isDefault && (
              <Badge variant="secondary" className="text-xs">
                默认
              </Badge>
            )}
            {labelConfig && !address.isDefault && (
              <Badge
                variant="secondary"
                className={`text-xs ${labelConfig.color}`}
              >
                {labelConfig.label}
              </Badge>
            )}
          </div>
          <p className="text-sm text-gray-600 line-clamp-2">
            {address.province} {address.city} {address.district} {address.detail}
          </p>
        </div>
      </div>

      {showActions && (
        <div className="flex items-center justify-end gap-4 mt-4 pt-4 border-t border-gray-100">
          {!address.isDefault && onSetDefault && (
            <button
              onClick={(e) => {
                e.stopPropagation();
                onSetDefault(address.id);
              }}
              className="text-sm text-gray-500 hover:text-black transition-colors"
            >
              设为默认
            </button>
          )}
          {onEdit && (
            <button
              onClick={(e) => {
                e.stopPropagation();
                onEdit(address.id);
              }}
              className="flex items-center gap-1 text-sm text-gray-500 hover:text-black transition-colors"
            >
              <Edit2 className="w-4 h-4" />
              编辑
            </button>
          )}
        </div>
      )}
    </motion.div>
  );
}
