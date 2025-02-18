import request from '@/utils/request';

// 立即购买
export function purchaseDirectly(data) {
    return request({
        url: '/cart/purchase',
        method: 'post',
        data: {
            productId: data.productId,
            quantity: data.quantity,
            price: data.price,
            productName: data.productName
        }
    });
}

// 添加到购物车
export function addToCart(data) {
    return request({
        url: '/cart/add',
        method: 'post',
        data: {
            productId: data.productId,
            quantity: data.quantity
        }
    });
} 