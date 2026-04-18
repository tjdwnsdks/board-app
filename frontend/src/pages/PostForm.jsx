import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { postApi } from '../api'
import './PostForm.css'

export default function PostForm() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = !!id

  const [form, setForm] = useState({ title: '', content: '' })
  const [loading, setLoading] = useState(isEdit)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!isEdit) return
    postApi.getDetail(id)
      .then(res => {
        setForm({ title: res.data.title, content: res.data.content })
        setLoading(false)
      })
      .catch(() => navigate('/posts'))
  }, [id])

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.title.trim() || !form.content.trim()) {
      setError('제목과 내용을 입력해주세요.')
      return
    }
    setSubmitting(true)
    setError('')
    try {
      if (isEdit) {
        await postApi.update(id, form)
        navigate(`/posts/${id}`)
      } else {
        const res = await postApi.create(form)
        navigate(`/posts/${res.data.id}`)
      }
    } catch (e) {
      setError(e.response?.data?.message || '저장 실패. 다시 시도해주세요.')
      setSubmitting(false)
    }
  }

  if (loading) return <div className="spinner" />

  return (
    <div className="post-form-page">
      <div className="page-header">
        <h1 className="page-title">{isEdit ? '게시글 수정' : '새 게시글 작성'}</h1>
      </div>

      <div className="card post-form-card">
        <form onSubmit={handleSubmit}>
          {error && <div className="alert alert-error">{error}</div>}

          <div className="form-group">
            <label className="form-label">제목 *</label>
            <input
              type="text"
              className="form-input"
              placeholder="제목을 입력하세요"
              value={form.title}
              onChange={e => setForm(f => ({ ...f, title: e.target.value }))}
              maxLength={200}
            />
            <div className="char-count">{form.title.length}/200</div>
          </div>

          <div className="form-group">
            <label className="form-label">내용 *</label>
            <textarea
              className="form-input"
              placeholder="내용을 입력하세요"
              value={form.content}
              onChange={e => setForm(f => ({ ...f, content: e.target.value }))}
              rows={14}
            />
          </div>

          <div className="form-actions">
            <button type="button" className="btn btn-outline" onClick={() => navigate(-1)}>취소</button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? '저장 중...' : isEdit ? '수정 완료' : '게시글 등록'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
